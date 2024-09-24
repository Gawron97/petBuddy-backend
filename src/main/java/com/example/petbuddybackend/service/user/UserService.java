package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
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
    private final ClientRepository clientRepository;
    private final CaretakerRepository caretakerRepository;

    @Transactional
    public AppUser createUserIfNotExistOrGet(JwtAuthenticationToken token) {

        String email = (String) token.getTokenAttributes().get("email");

        if(userRepository.findById(email).isEmpty()) {
            log.info("User with email: " + email + " not found. Creating new user.");
            AppUser user = createAppUser(email, token);
            log.info("User with email: " + email + " created.");
            return user;
        }
        return getAppUser(email);
    }

    private AppUser createAppUser(String email, JwtAuthenticationToken token) {
        AppUser user = AppUser.builder()
                .email(email)
                .name((String) token.getTokenAttributes().get("given_name"))
                .surname((String) token.getTokenAttributes().get("family_name"))
                .build();
        return userRepository.save(user);
    }

    public AppUser getAppUser(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
    }

    public UserProfiles getUserProfiles(String email) {
        return UserProfiles.builder()
                .email(email)
                .hasClientProfile(clientRepository.existsById(email))
                .hasCaretakerProfile(caretakerRepository.existsById(email))
                .build();
    }

}
