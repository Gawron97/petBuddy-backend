package com.example.petbuddybackend.entity.photo;

import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloudPhotoDeleteListener {

    private final FirebaseApp firebaseApp;

    @PreRemove
    public void removePhotoFromRemote(CloudPhoto photo) {
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        var bucket = storageClient.bucket();
        var blobToDelete = bucket.get(photo.getBlob());

        if(blobToDelete != null)
            blobToDelete.delete();
    }
}
