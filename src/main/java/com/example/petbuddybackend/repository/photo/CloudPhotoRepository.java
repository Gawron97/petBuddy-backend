package com.example.petbuddybackend.repository.photo;

import com.example.petbuddybackend.entity.photo.CloudPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudPhotoRepository extends JpaRepository<CloudPhoto, String> {
}
