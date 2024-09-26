package com.example.petbuddybackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @SecurityRequirements
    @GetMapping
    public CsrfToken csrfGeneration(CsrfToken token) {
        log.error("CsrfToken: {}", token.getToken());
        return token;
    }
}
