package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.ipn.mx.entity.Donador;
import com.ipn.mx.entity.Persona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DonadorServiceImpl implements DonadorService {

    private final Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private PersonaService personaService;

    @PostConstruct
    public void initializeDonadorCounter() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("DonadorCounter");
        counterRef.set(new HashMap<String, Object>() {{
            put("nextId", 1); // Inicializar con 1 para empezar
        }});
        System.out.println("Global Counter para Donador inicializado.");
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
    public Donador saveDonador(Integer centroId, Integer intermediarioId, Donador donador) {
        if (donador.getId() == null) {
            donador.setId(getNextId());
        }

        // Validar que el PersonaId sea de un donador
        Persona persona = personaService.getPersonaById(donador.getPersonaId());
        if (persona == null || !"Donador".equals(persona.getRol())) {
            throw new IllegalArgumentException("El PersonaId proporcionado no es válido o no corresponde a un donador.");
        }

        // Verificar que el PersonaId no esté asociado a otro donador
        DocumentReference personaMapRef = db.collection("PersonaDonadorMap").document(String.valueOf(donador.getPersonaId()));
        ApiFuture<DocumentSnapshot> future = personaMapRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                throw new IllegalArgumentException("El PersonaId proporcionado ya está asociado a otro donador.");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al verificar el PersonaId", e);
        }

        // Validar que los BeneficiarioIds pertenezcan al rol de beneficiario y obtener los IDs correctos
        List<Integer> validBeneficiarioIds = new ArrayList<>();
        for (Integer beneficiarioId : donador.getBeneficiarioIds()) {
            DocumentReference beneficiarioRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
                    .collection("Intermediario").document(String.valueOf(intermediarioId))
                    .collection("Beneficiario").document(String.valueOf(beneficiarioId));
            ApiFuture<DocumentSnapshot> futureBeneficiario = beneficiarioRef.get();
            try {
                DocumentSnapshot beneficiarioDocument = futureBeneficiario.get();
                if (beneficiarioDocument.exists()) {
                    validBeneficiarioIds.add(beneficiarioId);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al verificar los BeneficiarioIds", e);
            }
        }

        if (validBeneficiarioIds.size() != donador.getBeneficiarioIds().size()) {
            throw new IllegalArgumentException("Uno o más BeneficiarioIds no corresponden a beneficiarios válidos.");
        }

        donador.setBeneficiarioIds(validBeneficiarioIds);

        db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(donador.getId())).set(donador);

        // Guardar la asociación en PersonaDonadorMap
        HashMap<String, Object> personaMap = new HashMap<>();
        personaMap.put("donadorId", donador.getId());
        personaMapRef.set(personaMap);

        return donador;
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
                donador.setId(id); // Asegurar que el Id se mantenga
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
        try {
            DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
                .collection("Intermediario").document(String.valueOf(intermediarioId))
                .collection("Donador").document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Donador donador = document.toObject(Donador.class);
                db.collection("DeletedDonador").document(String.valueOf(donador.getId())).set(donador);
                docRef.delete().get();

                // Eliminar la asociación en PersonaDonadorMap
                db.collection("PersonaDonadorMap").document(String.valueOf(donador.getPersonaId())).delete();

                // Eliminar la persona asociada
                if (donador.getPersonaId() != null) {
                    personaService.deletePersonaById(donador.getPersonaId());
                }
                System.out.println("Deleted document with ID: " + id);
            } else {
                System.out.println("No document found with ID: " + id);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Error deleting document with ID: " + id);
        }
    }

    @Override
    public void addBeneficiarioToDonador(Integer centroId, Integer intermediarioId, Integer donadorId, Integer beneficiarioId) {
        // Validar que el PersonaId sea de un beneficiario
        Persona persona = personaService.getPersonaById(beneficiarioId);
        if (persona == null || !"Beneficiario".equals(persona.getRol())) {
            throw new IllegalArgumentException("El PersonaId proporcionado no es válido o no corresponde a un beneficiario.");
        }

        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Donador").document(String.valueOf(donadorId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Donador donador = document.toObject(Donador.class);
                List<Integer> beneficiarioIds = donador.getBeneficiarioIds();
                if (!beneficiarioIds.contains(beneficiarioId)) {
                    beneficiarioIds.add(beneficiarioId);
                    docRef.update("beneficiarioIds", beneficiarioIds);
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
                    docRef.update("beneficiarioIds", beneficiarioIds);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Donador> getDonadoresByIntermediarioId(Integer centroId, Integer intermediarioId) {
        List<Donador> donadores = new ArrayList<>();
        CollectionReference donadoresRef = db.collection("CentroDeDonacion")
                .document(String.valueOf(centroId))
                .collection("Intermediario")
                .document(String.valueOf(intermediarioId))
                .collection("Donador");
        try {
            ApiFuture<QuerySnapshot> future = donadoresRef.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot document : documents) {
                Donador donador = document.toObject(Donador.class);
                donador.setId(Integer.parseInt(document.getId()));
                donadores.add(donador);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return donadores;
    }
}
