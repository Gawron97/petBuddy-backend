package com.example.petbuddybackend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
@Slf4j
public class TestDataConfiguration {

    private final NecessaryDataCreator necessaryDataCreator;

    @PostConstruct
    public void setupTestData() {

        log.info("Setting up test data");
        necessaryDataCreator.createData();

    }


}
