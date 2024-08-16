package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
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
    private final CaretakerRepository caretakerRepository;

    @Transactional
    public void createUserIfNotExist(JwtAuthenticationToken token) {

        String email = (String) token.getTokenAttributes().get("email");

        if(userRepository.findById(email).isEmpty()) {
            log.info("User with email: " + email + " not found. Creating new user.");
            AppUser user = createAppUser(email, token);
            createCaretaker(user); // refactor needed but need to consult when caretaker profile should be created
                                   // now I mocked it to make task working
            log.info("User with email: " + email + " created.");
        }
    }

    private AppUser createAppUser(String email, JwtAuthenticationToken token) {
        AppUser user = AppUser.builder()
                .email(email)
                .name((String) token.getTokenAttributes().get("given_name"))
                .surname((String) token.getTokenAttributes().get("family_name"))
                .build();
        return userRepository.save(user);
    }

    private void createCaretaker(AppUser appUser) {
        Caretaker caretaker = Caretaker.builder()
                .email(appUser.getEmail())
                .accountData(appUser)
                .build();
        caretakerRepository.save(caretaker);
    }

}
