package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository userRepository;

    @Transactional
    public void createUserIfNotExist(JwtAuthenticationToken token) {

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
