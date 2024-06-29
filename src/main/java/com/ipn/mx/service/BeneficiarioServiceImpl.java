package com.ipn.mx.service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.ipn.mx.entity.Beneficiario;
import com.ipn.mx.entity.Persona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BeneficiarioServiceImpl implements BeneficiarioService {

    private final Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private PersonaService personaService;

    @Bean
    public void initializeBeneficiarioCounter() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("BeneficiarioCounter");
        counterRef.set(new HashMap<String, Object>() {{
            put("nextId", 1); // Inicializar con 1 para empezar
        }});
        System.out.println("Global Counter para Beneficiario inicializado.");
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
    public void saveBeneficiario(Integer centroId, Integer intermediarioId, Beneficiario beneficiario) {
        if (beneficiario.getId() == null) {
            beneficiario.setId(getNextId());
        }

        // Validar que el PersonaId sea de un beneficiario
        Persona persona = personaService.getPersonaById(beneficiario.getPersonaId());
        if (persona == null || !"Beneficiario".equals(persona.getRol())) {
            throw new IllegalArgumentException("El PersonaId proporcionado no es válido o no corresponde a un beneficiario.");
        }

        // Verificar que el PersonaId no esté asociado a otro beneficiario
        DocumentReference mapRef = db.collection("PersonaBeneficiarioMap").document(String.valueOf(beneficiario.getPersonaId()));
        ApiFuture<DocumentSnapshot> futureMap = mapRef.get();
        try {
            if (futureMap.get().exists()) {
                throw new IllegalArgumentException("El PersonaId proporcionado ya está asociado a otro beneficiario.");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al verificar el PersonaId", e);
        }

        // Validar que los DonadorIds pertenezcan al rol de donador y obtener los IDs correctos
        List<Integer> validDonadorIds = new ArrayList<>();
        for (Integer donadorId : beneficiario.getDonadorIds()) {
            DocumentReference donadorRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
                    .collection("Intermediario").document(String.valueOf(intermediarioId))
                    .collection("Donador").document(String.valueOf(donadorId));
            ApiFuture<DocumentSnapshot> futureDonador = donadorRef.get();
            try {
                DocumentSnapshot donadorDocument = futureDonador.get();
                if (donadorDocument.exists()) {
                    validDonadorIds.add(donadorId);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al verificar los DonadorIds", e);
            }
        }

        if (validDonadorIds.size() != beneficiario.getDonadorIds().size()) {
            throw new IllegalArgumentException("Uno o más DonadorIds no corresponden a donadores válidos.");
        }

        beneficiario.setDonadorIds(validDonadorIds);

        db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(beneficiario.getId())).set(beneficiario);

        // Guardar la asociación en PersonaBeneficiarioMap
        HashMap<String, Object> personaMap = new HashMap<>();
        personaMap.put("beneficiarioId", beneficiario.getId());
        mapRef.set(personaMap);
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
                beneficiario.setId(id); // Asegurar que el Id se mantenga
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
        try {
            DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
                .collection("Intermediario").document(String.valueOf(intermediarioId))
                .collection("Beneficiario").document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                db.collection("DeletedBeneficiario").document(String.valueOf(beneficiario.getId())).set(beneficiario);
                docRef.delete().get();

                // Eliminar la asociación en PersonaBeneficiarioMap
                db.collection("PersonaBeneficiarioMap").document(String.valueOf(beneficiario.getPersonaId())).delete();

                // Eliminar la persona asociada
                if (beneficiario.getPersonaId() != null) {
                    personaService.deletePersonaById(beneficiario.getPersonaId());
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
    public void addDonadorToBeneficiario(Integer centroId, Integer intermediarioId, Integer beneficiarioId, Integer donadorId) {
        // Validar que el PersonaId sea de un donador
        Persona persona = personaService.getPersonaById(donadorId);
        if (persona == null || !"Donador".equals(persona.getRol())) {
            throw new IllegalArgumentException("El PersonaId proporcionado no es válido o no corresponde a un donador.");
        }

        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediarioId))
            .collection("Beneficiario").document(String.valueOf(beneficiarioId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                List<Integer> donadorIds = beneficiario.getDonadorIds();
                if (!donadorIds.contains(donadorId)) {
                    donadorIds.add(donadorId);
                    docRef.update("donadorIds", donadorIds);
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
                    docRef.update("donadorIds", donadorIds);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Beneficiario> getBeneficiariosByIntermediarioId(Integer centroId, Integer intermediarioId) {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        CollectionReference beneficiariosRef = db.collection("CentroDeDonacion")
                .document(String.valueOf(centroId))
                .collection("Intermediario")
                .document(String.valueOf(intermediarioId))
                .collection("Beneficiario");
        try {
            ApiFuture<QuerySnapshot> future = beneficiariosRef.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot document : documents) {
                Beneficiario beneficiario = document.toObject(Beneficiario.class);
                beneficiario.setId(Integer.parseInt(document.getId()));
                beneficiarios.add(beneficiario);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return beneficiarios;
    }
}
