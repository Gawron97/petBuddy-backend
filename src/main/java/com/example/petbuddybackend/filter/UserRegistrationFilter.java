package com.example.petbuddybackend.filter;

import com.example.petbuddybackend.entity.AppUser;
import com.example.petbuddybackend.repository.AppUserRepository;
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

    private final AppUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(!"OPTIONS".equalsIgnoreCase((request.getMethod()))) {
            JwtAuthenticationToken token = (JwtAuthenticationToken) request.getUserPrincipal();
            log.info("Request by user with email: " + token.getTokenAttributes().get("email"));

            createUserIfNotExist(token);
        }

        filterChain.doFilter(request, response);

    }

    private void createUserIfNotExist(JwtAuthenticationToken token) {

        String email = (String) token.getTokenAttributes().get("email");

        if(userRepository.findById(email).isEmpty()) {
            log.info("User with email: " + email + " not found. Creating new user.");
            AppUser user = AppUser.builder()
                    .email(email)
                    .name((String) token.getTokenAttributes().get("given_name"))
                    .surname((String) token.getTokenAttributes().get("family_name"))
                    .username((String) token.getTokenAttributes().get("preferred_username"))
                    .build();
            userRepository.save(user);
            log.info("User with email: " + email + " created.");
        }

    }

}
