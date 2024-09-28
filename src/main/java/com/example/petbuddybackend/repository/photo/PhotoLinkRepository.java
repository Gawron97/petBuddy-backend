package com.example.petbuddybackend.repository.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoLinkRepository extends JpaRepository<PhotoLink, String> {
}
