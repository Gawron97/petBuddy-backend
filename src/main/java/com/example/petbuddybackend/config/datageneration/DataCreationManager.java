package com.example.petbuddybackend.config.datageneration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataCreationManager {

    private final NecessaryDataCreator necessaryDataCreator;
    private final MockDataCreator mockDataCreator;

    @PostConstruct
    public void createData() {
        necessaryDataCreator.createData();
        mockDataCreator.createMockData();
    }

}
