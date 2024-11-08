package com.example.petbuddybackend.testconfig;


import com.example.petbuddybackend.service.session.chat.ChatSessionTracker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class ChatSessionTrackerTestConfig {

    @Bean
    @Primary
    public ChatSessionTracker chatSessionTracker() {
        return new ChatSessionTracker();
    }
}
