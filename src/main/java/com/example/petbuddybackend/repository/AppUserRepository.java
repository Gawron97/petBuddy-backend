package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
}
