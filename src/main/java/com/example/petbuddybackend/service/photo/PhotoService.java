package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PhotoService {

    PhotoLink uploadPhoto(MultipartFile multipartFile);

    List<PhotoLink> uploadPhotos(List<MultipartFile> multipartFiles);

    @Async
    void schedulePhotoDeletion(PhotoLink photoLink);

    /**
     * Schedules deletion of multiple photos asynchronously.
     * */
    void schedulePhotoDeletions(Collection<PhotoLink> photoLinksToDelete);

    PhotoLink updatePhotoExpiration(PhotoLink photo);

    List<PhotoLink> updatePhotoExpirations(List<PhotoLink> photos);

    Optional<PhotoLink> findPhotoLinkByNullableId(String blob);
}
