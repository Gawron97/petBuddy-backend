package com.example.petbuddybackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @SecurityRequirements
    @GetMapping
    public void csrfGeneration() {
        // This endpoint is used to generate a CSRF token
    }

}
