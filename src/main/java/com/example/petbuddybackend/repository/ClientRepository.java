package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.user.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c.id FROM Client c WHERE c.accountData.username = :username")
    Optional<Long> findClientIdByUsername(String username);
}
