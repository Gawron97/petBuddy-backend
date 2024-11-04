package com.example.petbuddybackend.repository.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoLinkRepository extends JpaRepository<PhotoLink, String> {

    List<PhotoLink> getAllByMarkedForDeletionAtNotNull();
}
