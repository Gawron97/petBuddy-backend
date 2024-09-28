package com.example.petbuddybackend.config.firebase;

import com.google.cloud.storage.Blob;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class FirebasePhotosCleaner  {

    private static final String CREATE_DROP = "create-drop";
    private static final String SPRING_JPA_HIBERNATE_DDL_AUTO = "spring.jpa.hibernate.ddl-auto";

    @Value("${firebase.photo.directory}")
    private String PHOTO_DIRECTORY;

    private final FirebaseApp firebaseApp;
    private final Environment environment;

    @PreDestroy
    @PostConstruct
    public void photosCleanup() {
        if (isCreateDropEnabled()) {
            log.warn("Firebase storage cleanup is enabled. Cleaning up...");
            cleanupPhotosFromFirebase();
        }
    }

    private boolean isCreateDropEnabled() {
        String ddlAuto = environment.getProperty(SPRING_JPA_HIBERNATE_DDL_AUTO);
        return CREATE_DROP.equalsIgnoreCase(ddlAuto);
    }

    private void cleanupPhotosFromFirebase() {
        log.info("Cleaning Firebase storage...");
        AtomicInteger successCount = new AtomicInteger();
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        Iterator<Blob> blobs = storageClient.bucket()
                .list()
                .iterateAll()
                .iterator();

        blobs.forEachRemaining(blob -> {
            try {
                if (blob.getName().startsWith(PHOTO_DIRECTORY)) {
                    blob.delete();
                    log.trace("Deleted photo from Firebase: {}", blob.getName());
                    successCount.getAndIncrement();
                }
            } catch (Exception e) {
                log.error("Error while deleting photo: {}", blob.getName(), e);
            }
        });

        log.info("Deleted {} photos from Firebase storage", successCount.get());
    }
}
