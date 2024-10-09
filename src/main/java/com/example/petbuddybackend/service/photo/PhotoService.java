package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PhotoService {

    PhotoLink uploadPhoto(MultipartFile multipartFile);

    Set<PhotoLink> uploadPhotos(List<MultipartFile> multipartFiles);

    void deletePhoto(String blob);

    void deletePhoto(PhotoLink photoLink);

    void deletePhotos(Set<PhotoLink> photoLinksToDelete);

    Set<PhotoLink> patchPhotos(Set<PhotoLink> currentPhotos, Set<String> blobsToKeep, List<MultipartFile> newPhotos);

    PhotoLink updatePhotoExpiration(PhotoLink photo);

    Set<PhotoLink> updatePhotoExpirations(Set<PhotoLink> photos);

    Optional<PhotoLink> findPhotoLinkByNullableId(String blob);
}
