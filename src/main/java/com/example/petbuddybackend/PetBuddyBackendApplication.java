package com.example.petbuddybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetBuddyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetBuddyBackendApplication.class, args);
    }

}
