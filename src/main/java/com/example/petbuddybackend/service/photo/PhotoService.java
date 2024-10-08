package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;

public interface PhotoService {

    PhotoLink uploadPhoto(MultipartFile multipartFile);

    void deletePhoto(String blob);

    void deletePhoto(PhotoLink photoLink);

    PhotoLink updatePhotoExpiration(PhotoLink photo);

    Set<PhotoLink> updatePhotoExpirations(Set<PhotoLink> photos);

    Optional<PhotoLink> findPhotoLinkByNullableId(String blob);
}
