package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.ipn.mx.entity.CentroDeDonacion;
import com.ipn.mx.entity.Intermediario;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CentroDeDonacionServiceImpl implements CentroDeDonacionService {

    private final Firestore db = FirestoreClient.getFirestore();
    private final IntermediarioService intermediarioService;

    public CentroDeDonacionServiceImpl(IntermediarioService intermediarioService) {
        this.intermediarioService = intermediarioService;
    }

    @Bean
    public void initializeGlobalCounter() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("CentroDeDonacionCounter");
        counterRef.set(new HashMap<String, Object>() {{
            put("nextId", 1); // Inicializar con 1 para empezar
        }});
        System.out.println("Global Counter initialized.");
    }

    private Integer getNextId() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("CentroDeDonacionCounter");

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
    public void saveCentro(CentroDeDonacion centro) {
        centro.setId(getNextId()); // Asignar siempre un nuevo ID
        db.collection("CentroDeDonacion").document(String.valueOf(centro.getId())).set(centro);
    }

    @Override
    public CentroDeDonacion getCentroById(Integer id) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(CentroDeDonacion.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CentroDeDonacion> getAllCentros() {
        List<CentroDeDonacion> centros = new ArrayList<>();
        CollectionReference centrosRef = db.collection("CentroDeDonacion");
        try {
            ApiFuture<QuerySnapshot> future = centrosRef.get();
            future.get().getDocuments().forEach(document -> centros.add(document.toObject(CentroDeDonacion.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch centros", e);
        }
        return centros;
    }

    @Override
    public CentroDeDonacion updateCentro(Integer id, CentroDeDonacion centro) {
        DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                centro.setId(id);
                docRef.set(centro);
                return centro;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteCentroById(Integer id) {
        try {
            DocumentReference docRef = db.collection("CentroDeDonacion").document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                CentroDeDonacion centro = document.toObject(CentroDeDonacion.class);
                db.collection("DeletedCentroDeDonacion").document(String.valueOf(centro.getId())).set(centro);
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

        deleteIntermediariosByCentroId(Integer.parseInt(docRef.getId()));
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

    private void deleteIntermediariosByCentroId(Integer centroId) throws ExecutionException, InterruptedException {
        List<Intermediario> intermediarios = intermediarioService.getIntermediariosByCentroId(centroId);
        for (Intermediario intermediario : intermediarios) {
            intermediarioService.deleteIntermediarioById(centroId, intermediario.getId());
        }
    }
}
