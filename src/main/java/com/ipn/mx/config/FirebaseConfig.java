package com.ipn.mx.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.url}")
    private String firebaseConfigUrl;

    @Bean
    public FirebaseApp createFirebaseApp() throws Exception {
        try {
            InputStream serviceAccount = new URL(firebaseConfigUrl).openStream();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize FirebaseApp", e);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        Firestore db = FirestoreClient.getFirestore(firebaseApp);
        initializeGlobalCounter(db);
        return db;
    }

    private void initializeGlobalCounter(Firestore db) {
        DocumentReference counterRef = db.collection("GlobalCounters").document("CentroDeDonacionCounter");
        try {
            DocumentSnapshot snapshot = counterRef.get().get();
            if (!snapshot.exists()) {
                counterRef.set(new HashMap<String, Object>() {{
                    put("nextId", 1);
                }});
                System.out.println("Global Counter initialized.");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize global counter", e);
        }
    }
}
