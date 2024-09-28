package com.example.petbuddybackend.testconfig;

import com.google.firebase.FirebaseApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class FirebaseMockBean {

    @Bean
    public FirebaseApp firebaseAppMock() {
        return mock(FirebaseApp.class);
    }
}
