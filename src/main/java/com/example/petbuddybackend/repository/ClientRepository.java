package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.user.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, String> {
}
