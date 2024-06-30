package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ipn.mx.entity.Donador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DonadorServiceImpl implements DonadorService {

    private final Firestore db;

    @Autowired
    public DonadorServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Donador saveDonador(Integer centroId, Integer intermediarioId, Donador donador) {
        if (donador.getId() == null) {
            donador.setId(getNextId());
        }

        if (!validatePersonaId(donador.getPersonaId())) {
            throw new IllegalArgumentException("PersonaId no existe o no tiene el rol adecuado");
        }

        for (Integer beneficiarioId : donador.getBeneficiarioIds()) {
            if (!validatePersonaId(beneficiarioId)) {
                throw new IllegalArgumentException("BeneficiarioId " + beneficiarioId + " no existe o no tiene el rol adecuado");
            }
        }

        db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(donador.getId())).set(donador);
        return donador;
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
        DocumentReference counterRef = db.collection("GlobalCounters").document("DonadorCounter");

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
    public Donador getDonadorById(Integer centroId, Integer intermediarioId, Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Donador.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Donador> getAllDonadores(Integer centroId, Integer intermediarioId) {
        List<Donador> donadores = new ArrayList<>();
        CollectionReference donadoresRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador");
        try {
            ApiFuture<QuerySnapshot> future = donadoresRef.get();
            List<Donador> finalDonadores = donadores;
            future.get().getDocuments().forEach(document -> finalDonadores.add(document.toObject(Donador.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch donadores", e);
        }
        return donadores;
    }

    @Override
    public Donador updateDonador(Integer centroId, Integer intermediarioId, Integer id, Donador donador) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                donador.setId(id);
                docRef.set(donador);
                return donador;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteDonadorById(Integer centroId, Integer intermediarioId, Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(id));
        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Donador donador = document.toObject(Donador.class);
                db.collection("DeletedDonador").document(String.valueOf(donador.getId())).set(donador);
                docRef.delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBeneficiarioToDonador(Integer centroId, Integer intermediarioId, Integer donadorId, Integer beneficiarioId) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(donadorId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Donador donador = document.toObject(Donador.class);
                List<Integer> beneficiarioIds = donador.getBeneficiarioIds();
                if (!beneficiarioIds.contains(beneficiarioId) && validatePersonaId(beneficiarioId)) {
                    beneficiarioIds.add(beneficiarioId);
                    donador.setBeneficiarioIds(beneficiarioIds);
                    docRef.set(donador);
                } else {
                    throw new IllegalArgumentException("BeneficiarioId no existe o no tiene el rol adecuado");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBeneficiarioFromDonador(Integer centroId, Integer intermediarioId, Integer donadorId, Integer beneficiarioId) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(donadorId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Donador donador = document.toObject(Donador.class);
                List<Integer> beneficiarioIds = donador.getBeneficiarioIds();
                if (beneficiarioIds.contains(beneficiarioId)) {
                    beneficiarioIds.remove(beneficiarioId);
                    donador.setBeneficiarioIds(beneficiarioIds);
                    docRef.set(donador);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Donador> getDonadoresByIntermediarioId(Integer centroId, Integer intermediarioId) {
        return getAllDonadores(centroId, intermediarioId);
    }
}
