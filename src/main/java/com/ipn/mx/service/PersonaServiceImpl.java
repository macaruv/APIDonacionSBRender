package com.ipn.mx.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.ipn.mx.entity.Persona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final Firestore db;

    @Autowired
    public PersonaServiceImpl(Firestore db) {
        this.db = db;
    }

    @Override
    public Persona savePersona(Persona persona) {
        if (persona.getId() == null) {
            persona.setId(getNextId());
        }
        db.collection("Persona").document(String.valueOf(persona.getId())).set(persona);
        return persona;
    }

    private Integer getNextId() {
        DocumentReference counterRef = db.collection("GlobalCounters").document("PersonaCounter");

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
    public Persona getPersonaById(Integer id) {
        DocumentReference docRef = db.collection("Persona").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Persona.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Persona> getAllPersonas() {
        List<Persona> personas = new ArrayList<>();
        CollectionReference personasRef = db.collection("Persona");
        try {
            ApiFuture<QuerySnapshot> future = personasRef.get();
            List<Persona> finalPersonas = personas;
            future.get().getDocuments().forEach(document -> finalPersonas.add(document.toObject(Persona.class)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch personas", e);
        }
        return personas;
    }

    @Override
    public Persona updatePersona(Integer id, Persona persona) {
        DocumentReference docRef = db.collection("Persona").document(String.valueOf(id));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                persona.setId(id);
                docRef.set(persona);
                return persona;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deletePersonaById(Integer id) {
        DocumentReference docRef = db.collection("Persona").document(String.valueOf(id));
        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Persona persona = document.toObject(Persona.class);
                db.collection("DeletedPersona").document(String.valueOf(persona.getId())).set(persona);
                docRef.delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
