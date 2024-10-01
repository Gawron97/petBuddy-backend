package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    PhotoLink uploadPhoto(MultipartFile multipartFile);

    void deletePhoto(String blob);

    void deletePhoto(PhotoLink photoLink);

    PhotoLink updatePhotoExpiration(PhotoLink photo);

    PhotoLink getPhoto(String blob);
}
