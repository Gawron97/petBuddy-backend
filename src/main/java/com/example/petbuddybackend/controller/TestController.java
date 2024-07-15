package com.example.petbuddybackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

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
}
