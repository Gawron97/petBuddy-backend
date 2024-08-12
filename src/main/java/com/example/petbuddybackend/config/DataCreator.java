package com.example.petbuddybackend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataCreator {

    private final NecessaryDataCreator necessaryDataCreator;
    private final MockDataCreator mockDataCreator;

    @PostConstruct
    public void createData() {
        necessaryDataCreator.createData();
        mockDataCreator.createMockData();
    }

}
