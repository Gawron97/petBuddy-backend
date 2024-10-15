package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.service.notification.WebsocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@Deprecated(forRemoval = true)
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final WebsocketNotificationService websocketNotificationService;

    @GetMapping
    public List<String> test() {
        return new ArrayList<>(List.of("test1", "test2"));
    }

    @GetMapping("/redirect")
    public String redirectSuccessful() {
        return "redirected successfully";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @GetMapping("/exception")
    public String exception() {
        throw new RuntimeException("Test exception thrown");
    }

    @PostMapping
    public List<String> postTest() {
        return new ArrayList<>(List.of("posttest1", "posttest2"));
    }

    @GetMapping("/send-notification/jg")
    public void sendNotification() {
        websocketNotificationService.sendNotification(
                "jakubgawron97@gmail.com",
                ClientNotification.builder()
                        .id(1L)
                        .objectId(1L)
                        .objectType(ObjectType.CARE)
                        .message("Test message")
                        .createdAt(ZonedDateTime.now())
                        .isRead(false)
                        .build()
        );
    }

    @GetMapping("/send-notification/stud")
    public void sendNotificationStud() {
        websocketNotificationService.sendNotification(
                "266850@student.pwr.edu.pl",
                ClientNotification.builder()
                        .id(1L)
                        .objectId(1L)
                        .objectType(ObjectType.CARE)
                        .message("Test message")
                        .createdAt(ZonedDateTime.now())
                        .isRead(false)
                        .build()
        );
    }
}