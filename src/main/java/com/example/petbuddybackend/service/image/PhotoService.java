package com.example.petbuddybackend.service.image;

import com.example.petbuddybackend.entity.photo.CloudPhoto;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    CloudPhoto uploadPhoto(MultipartFile multipartFile);

    void deletePhoto(String blob);

    CloudPhoto updatePhotoExpiration(CloudPhoto photo);

    CloudPhoto getPhoto(String blob);
}
