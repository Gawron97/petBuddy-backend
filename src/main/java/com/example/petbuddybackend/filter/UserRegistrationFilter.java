package com.example.petbuddybackend.filter;

import com.example.petbuddybackend.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@Configuration
@EnableWebMvc
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getUserPrincipal() instanceof JwtAuthenticationToken token ) {
            log.info("Request by user with email: " + token.getTokenAttributes().get("email"));
            userService.createUserIfNotExist(token);
        }

        filterChain.doFilter(request, response);
    }
}
