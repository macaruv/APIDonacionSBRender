package com.ipn.mx.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.net.URL;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.url}")
    private String firebaseConfigUrl;

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        InputStream serviceAccount = new URL(firebaseConfigUrl).openStream();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
