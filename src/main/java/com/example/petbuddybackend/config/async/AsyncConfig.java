package com.example.petbuddybackend.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("dev | prod")
@EnableAsync
@EnableScheduling
@Configuration
public class AsyncConfig implements AsyncConfigurer {
}
