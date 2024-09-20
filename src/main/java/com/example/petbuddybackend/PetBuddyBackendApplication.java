package com.example.petbuddybackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
@EnableScheduling
public class PetBuddyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetBuddyBackendApplication.class, args);
    }

}
