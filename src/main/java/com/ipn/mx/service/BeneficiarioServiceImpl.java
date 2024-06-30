package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ipn.mx.entity.Beneficiario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BeneficiarioServiceImpl implements BeneficiarioService {

    private final Firestore db;

    @Autowired
    public BeneficiarioServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Beneficiario saveBeneficiario(Integer centroId, Integer intermediarioId, Beneficiario beneficiario) {
        if (beneficiario.getId() == null) {
            beneficiario.setId(getNextId());
        }

        if (!validatePersonaId(beneficiario.getPersonaId())) {
            throw new IllegalArgumentException("PersonaId no existe o no tiene el rol adecuado");
        }

        for (Integer donadorId : beneficiario.getDonadorIds()) {
            if (!validatePersonaId(donadorId)) {
                throw new IllegalArgumentException("DonadorId " + donadorId + " no existe o no tiene el rol adecuado");
            }
        }

        db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(beneficiario.getId())).set(beneficiario);
        return beneficiario;
    }

    private boolean validatePersonaId(Integer personaId) {
        DocumentReference docRef = db.collection("Persona").document(String.valueOf(personaId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                String rol = document.getString("rol");
                return "Donador".equals(rol) || "Beneficiario".equals(rol);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer getNextId() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("BeneficiarioCounter");

        ApiFuture<Integer> transactionFuture = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();
            Integer nextId = snapshot.getLong("nextId").intValue();
            transaction.update(counterRef, "nextId", nextId + 1);
            return nextId;
        });

        try {
            return transactionFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener el siguiente ID", e);
        }
    }

    @Override
    public Beneficiario getBeneficiarioById(Integer centroId, Integer intermediarioId, Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Beneficiario.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Beneficiario> getAllBeneficiarios(Integer centroId, Integer intermediarioId) {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        CollectionReference beneficiariosRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario");
        try {
            ApiFuture<QuerySnapshot> future = beneficiariosRef.get();
            List<Beneficiario> finalBeneficiarios = beneficiarios;
            future.get().getDocuments().forEach(document -> finalBeneficiarios.add(document.toObject(Beneficiario.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch beneficiarios", e);
        }
        return beneficiarios;
    }

    @Override
    public Beneficiario updateBeneficiario(Integer centroId, Integer intermediarioId, Integer id, Beneficiario beneficiario) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                beneficiario.setId(id);
                docRef.set(beneficiario);
                return beneficiario;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteBeneficiarioById(Integer centroId, Integer intermediarioId, Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(id));
        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                db.collection("DeletedBeneficiario").document(String.valueOf(beneficiario.getId())).set(beneficiario);
                docRef.delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDonadorToBeneficiario(Integer centroId, Integer intermediarioId, Integer beneficiarioId, Integer donadorId) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(beneficiarioId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                List<Integer> donadorIds = beneficiario.getDonadorIds();
                if (!donadorIds.contains(donadorId) && validatePersonaId(donadorId)) {
                    donadorIds.add(donadorId);
                    beneficiario.setDonadorIds(donadorIds);
                    docRef.set(beneficiario);
                } else {
                    throw new IllegalArgumentException("DonadorId no existe o no tiene el rol adecuado");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDonadorFromBeneficiario(Integer centroId, Integer intermediarioId, Integer beneficiarioId, Integer donadorId) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(beneficiarioId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                List<Integer> donadorIds = beneficiario.getDonadorIds();
                if (donadorIds.contains(donadorId)) {
                    donadorIds.remove(donadorId);
                    beneficiario.setDonadorIds(donadorIds);
                    docRef.set(beneficiario);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Beneficiario> getBeneficiariosByIntermediarioId(Integer centroId, Integer intermediarioId) {
        return getAllBeneficiarios(centroId, intermediarioId);
    }
}
