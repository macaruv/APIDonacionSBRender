package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.ipn.mx.entity.Intermediario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class IntermediarioServiceImpl implements IntermediarioService {

    private final Firestore db;

    @Autowired
    public IntermediarioServiceImpl(Firestore db) {
        this.db = db;
    }

    @PostConstruct
    public void initializeIntermediarioCounter() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("IntermediarioCounter");
        counterRef.set(new HashMap<String, Object>() {{
            put("nextId", 1); // Inicializar con 1 para empezar
        }});
        System.out.println("Global Counter for Intermediario initialized.");
    }

    private Integer getNextId() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("IntermediarioCounter");

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
    public void saveIntermediario(Integer centroId, Intermediario intermediario) {
        if (intermediario.getId() == null) {
            intermediario.setId(getNextId());
        }
        intermediario.setCentroId(centroId); // Asignar el centroId al intermediario
        db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(intermediario.getId())).set(intermediario);
    }

    @Override
    public Intermediario getIntermediarioById(Integer centroId, Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Intermediario.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Intermediario> getAllIntermediarios(Integer centroId) {
        List<Intermediario> intermediarios = new ArrayList<>();
        CollectionReference intermediariosRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario");
        try {
            ApiFuture<QuerySnapshot> future = intermediariosRef.get();
            List<Intermediario> finalIntermediarios = intermediarios;
            future.get().getDocuments().forEach(document -> finalIntermediarios.add(document.toObject(Intermediario.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch intermediarios", e);
        }
        return intermediarios;
    }

    @Override
    public Intermediario updateIntermediario(Integer centroId, Integer id, Intermediario intermediario) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
            	intermediario.setId(id);
                intermediario.setCentroId(centroId); // Asegurar que el centroId se mantenga al actualizar
                docRef.set(intermediario);
                return intermediario;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteIntermediarioById(Integer centroId, Integer id) {
        try {
            DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
                .collection("Intermediario").document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Intermediario intermediario = document.toObject(Intermediario.class);
                db.collection("DeletedIntermediario").document(String.valueOf(intermediario.getId())).set(intermediario);
                deleteDocumentAndDependencies(docRef);
                docRef.delete().get();
                System.out.println("Deleted document with ID: " + id);
            } else {
                System.out.println("No document found with ID: " + id);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Error deleting document with ID: " + id);
        }
    }

    private void deleteDocumentAndDependencies(DocumentReference docRef) throws ExecutionException, InterruptedException {
        for (String collectionName : getDependentCollections()) {
            deleteCollection(docRef.collection(collectionName), 10);
        }

        docRef.delete().get();
    }

    private List<String> getDependentCollections() {
        return List.of("Beneficiario", "Donador");
    }

    private void deleteCollection(CollectionReference collection, int batchSize) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
        int deleted = 0;
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (DocumentSnapshot document : documents) {
            deleteDocumentAndDependencies(document.getReference());
            deleted++;
        }
        if (deleted >= batchSize) {
            deleteCollection(collection, batchSize);
        }
    }

    @Override
    public List<Intermediario> getIntermediariosByCentroId(Integer centroId) {
        List<Intermediario> intermediarios = new ArrayList<>();
        CollectionReference intermediariosRef = db.collection("CentroDeDonacion").document(String.valueOf(centroId))
            .collection("Intermediario");
        try {
            ApiFuture<QuerySnapshot> future = intermediariosRef.get();
            List<Intermediario> finalIntermediarios = intermediarios;
            future.get().getDocuments().forEach(document -> finalIntermediarios.add(document.toObject(Intermediario.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch intermediarios by centroId", e);
        }
        return intermediarios;
    }
}
