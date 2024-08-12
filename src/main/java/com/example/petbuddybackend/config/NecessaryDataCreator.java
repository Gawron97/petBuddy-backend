package com.example.petbuddybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class NecessaryDataCreator {

    public void createData() {

    }

}
