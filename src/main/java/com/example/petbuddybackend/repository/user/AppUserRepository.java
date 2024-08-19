package com.example.petbuddybackend.repository.user;

import com.example.petbuddybackend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
}
