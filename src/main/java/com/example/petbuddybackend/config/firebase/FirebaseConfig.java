package com.example.petbuddybackend.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import java.io.*;

@Slf4j
@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${firebase.key.path}")
    private String FIREBASE_SERVICE_ACCOUNT_KEY_PATH;

    @Value("${FIREBASE_APPLICATION_CREDENTIALS:#{null}}")
    private String FIREBASE_SERVICE_ACCOUNT_KEY_ENV_VARIABLE;

    @Value("${firebase.bucket.link}")
    private String FIREBASE_BUCKET_LINK;

    @Value("${firebase.project.id}")
    private String FIREBASE_PROJECT_ID;

    private static boolean initialized = false;

    @Bean
    public FirebaseApp firebaseApp() {
        if(initialized) {
            log.info("Firebase SDK already initialized!");
            return FirebaseApp.getInstance();
        }

        log.info("Initializing Firebase SDK...");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(createCredentials())
                .setStorageBucket(FIREBASE_BUCKET_LINK)
                .setProjectId(FIREBASE_PROJECT_ID)
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("Firebase SDK initialized!");
        initialized = true;
        return app;
    }

    private GoogleCredentials createCredentials() {
        File firebaseCredentialsFile = new File(FIREBASE_SERVICE_ACCOUNT_KEY_PATH);

        return firebaseCredentialsFile.exists() ?
                createCredentialsFromFile(firebaseCredentialsFile) :
                createCredentialsFromEnvVariable();
    }

    private GoogleCredentials createCredentialsFromFile(File file) {
        log.info("Reading Firebase service account key file...");

        try(InputStream serviceAccount = new FileInputStream(file)) {
            return GoogleCredentials.fromStream(serviceAccount);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading Firebase service account key file", e);
        }
    }

    private GoogleCredentials createCredentialsFromEnvVariable() {
        log.info("Reading Firebase service account key from env variable...");

        if(!StringUtils.hasText(FIREBASE_SERVICE_ACCOUNT_KEY_ENV_VARIABLE)) {
            throw new RuntimeException("Firebase service account key is not provided as env variable");
        }

        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(FIREBASE_SERVICE_ACCOUNT_KEY_ENV_VARIABLE.getBytes())) {
            return GoogleCredentials.fromStream(inputStream);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading Firebase service account key from env variable", e);
        }
    }
}