package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.user.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT c.email FROM Client c WHERE c.accountData.email = :email")
    Optional<Long> findClientIdByEmail(String email);
}
