package com.example.petbuddybackend;

import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetBuddyBackendApplication implements CommandLineRunner {

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    public static void main(String[] args) {
        SpringApplication.run(PetBuddyBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("sa");
    }
}