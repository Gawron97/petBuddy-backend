package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.repository.photo.PhotoLinkRepository;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.photo.InvalidPhotoException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePhotoService implements PhotoService {

    private final static String MISSING_FILE_EXTENSION = "Missing file extension";
    private final static String PHOTO = "Photo";
    private static final Set<String> acceptedImageTypes = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    @Value("${firebase.photo.directory}")
    private String PHOTO_DIRECTORY;

    @Value("${firebase.photo.expiration.max-seconds}")
    private Integer MAX_EXPIRATION_SECONDS;

    @Value("${firebase.photo.expiration.threshold-seconds}")
    private Integer EXPIRATION_THRESHOLD_SECONDS;

    private final FirebaseApp firebaseApp;
    private final PhotoLinkRepository photoRepository;
    private final Tika tika;


    @Override
    public Optional<PhotoLink> findPhotoLinkByNullableId(String blob) {
        if(blob == null) {
            return Optional.empty();
        }

        Optional<PhotoLink> photo = photoRepository.findById(blob);
        photo.ifPresent(this::updatePhotoExpiration);
        return photo;
    }

    @Override
    public PhotoLink uploadPhoto(MultipartFile multipartFile) {
        validatePhoto(multipartFile);
        PhotoLink photo = uploadFile(multipartFile, MAX_EXPIRATION_SECONDS);
        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public Set<PhotoLink> uploadPhotos(List<MultipartFile> multipartFiles) {
        multipartFiles.forEach(this::validatePhoto);
        Set<PhotoLink> uploadedPhotos = new HashSet<>(multipartFiles.size());

        try {
            for(MultipartFile file : multipartFiles) {
                uploadedPhotos.add(uploadFile(file, MAX_EXPIRATION_SECONDS));
            }
        } catch(Exception e) {
            // Rollback uploaded photos
            log.error("Error occurred while uploading photos. Rolling back uploaded photos", e);
            photoRepository.deleteAll(uploadedPhotos);
            throw e;
        }

        // TODO: maybe above rollback is not needed. Tests required
        return new HashSet<>(photoRepository.saveAll(uploadedPhotos));
    }

    @Override
    @Transactional
    public void deletePhoto(String blob) {
        Optional<PhotoLink> photo = findPhotoLinkByNullableId(blob);

        if(photo.isEmpty()) {
            return;
        }

        deletePhoto(photo.get());
    }

    @Override
    @Transactional
    public void deletePhoto(PhotoLink photoLink) {
        String blob = photoLink.getBlob();
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        Bucket bucket = storageClient.bucket();
        Blob blobToDelete = bucket.get(blob);

        if(blobToDelete != null) {
            blobToDelete.delete();
        }

        photoRepository.delete(photoLink);
    }

    @Override
    public void deletePhotos(Set<PhotoLink> photoLinksToDelete) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: implement
    }

    @Override
    @Transactional
    public Set<PhotoLink> patchPhotos(
            Set<PhotoLink> currentPhotos,
            Set<String> blobsToKeep,
            List<MultipartFile> newPhotos
    ) {
        int currentPhotosSize = currentPhotos.size();
        int photosToKeepSize = Math.min(currentPhotosSize, blobsToKeep.size() + newPhotos.size());
        int photosToRemoveSize = Math.max(0, currentPhotosSize - photosToKeepSize);

        Set<PhotoLink> photosToKeep = new HashSet<>(photosToKeepSize);
        Set<PhotoLink> photosToRemove = new HashSet<>(photosToRemoveSize);

        currentPhotos.forEach(photo -> {
            if(blobsToKeep.contains(photo.getBlob())) {
                photosToKeep.add(photo);
            } else {
                photosToRemove.add(photo);
            }
        });

        deletePhotos(photosToRemove);
        photosToKeep.addAll(uploadPhotos(newPhotos));
        return new HashSet<>(photoRepository.saveAll(photosToKeep));
    }

    @Override
    public PhotoLink updatePhotoExpiration(PhotoLink photo) {
        return photoRepository.save(applyPhotoRenewal(photo));
    }

    @Override
    public Set<PhotoLink> updatePhotoExpirations(Set<PhotoLink> photos) {
        List<PhotoLink> photosRenewed = photos.stream()
                .map(this::applyPhotoRenewal)
                .toList();

        return new HashSet<>(photoRepository.saveAll(photosRenewed));
    }

    /**
     * Checks if the provided file is not empty and if its type matches the accepted types defined in the acceptedImageTypes set.
     * */
     private void validatePhoto(MultipartFile multipartFile) {
         if (multipartFile == null || multipartFile.isEmpty()) {
             throw InvalidPhotoException.ofEmptyPhoto();
         }

         try(InputStream inputStream = multipartFile.getInputStream()) {
             String mimeType = tika.detect(inputStream);

             if(!acceptedImageTypes.contains(mimeType)) {
                 throw InvalidPhotoException.ofPhotoWithInvalidExtension(mimeType, acceptedImageTypes);
             }

         } catch (IOException e) {
             throw InvalidPhotoException.ofInvalidPhoto(multipartFile.getOriginalFilename());
         }
     }

    /**
     * @return Extension of the file like .jpg, .png, .jpeg
     * */
    private String getExtension(String fileName) {
        if(fileName == null) {
            throw new InvalidPhotoException(MISSING_FILE_EXTENSION);
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

    private PhotoLink uploadFile(MultipartFile file, int expirationSeconds) {
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        Bucket bucket = storageClient.bucket();
        String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        String blobPath = PHOTO_DIRECTORY + "/" + filename;

        try {
            Blob blob = bucket.create(blobPath, file.getInputStream(), file.getContentType());
            String url = blob.signUrl(expirationSeconds, TimeUnit.SECONDS).toString();

            return PhotoLink.builder()
                    .url(url)
                    .blob(blobPath)
                    .urlExpiresAt(LocalDateTime.now().plusSeconds(expirationSeconds))
                    .build();
        } catch(IOException e) {
            throw InvalidPhotoException.ofInvalidPhoto(file.getOriginalFilename());
        }
    }

    private String renewPhoto(String blobName, int expirationSeconds) {
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        Bucket bucket = storageClient.bucket();
        Blob blob = bucket.get(blobName);

        if(blob == null) {
            throw NotFoundException.withFormattedMessage(PHOTO, blobName);
        }

        return blob.signUrl(expirationSeconds, TimeUnit.SECONDS).toString();
    }

    private PhotoLink applyPhotoRenewal(PhotoLink photo) {
        LocalDateTime thresholdTime = photo
                .getUrlExpiresAt()
                .minusSeconds(EXPIRATION_THRESHOLD_SECONDS);

        if(thresholdTime.isAfter(LocalDateTime.now())) {
            return photo;
        }

        String newUrl = renewPhoto(photo.getBlob(), MAX_EXPIRATION_SECONDS);
        photo.setUrl(newUrl);
        photo.setUrlExpiresAt(LocalDateTime.now().plusSeconds(MAX_EXPIRATION_SECONDS));
        return photo;
    }
}
