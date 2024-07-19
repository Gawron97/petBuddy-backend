package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.testutils.MockUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AppUserTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @BeforeEach
    void init() {
        Caretaker caretaker = MockUtils.createMockCaretaker();

        AppUser user = AppUser.builder()
                .email(caretaker.getEmail())
                .build();

        caretaker.setAccountData(user);
        user.setCaretaker(caretaker);

        appUserRepository.saveAndFlush(user);
        caretakerRepository.saveAndFlush(caretaker);
    }

    @AfterEach
    void cleanup() {
        appUserRepository.deleteAll();
        caretakerRepository.deleteAll();
    }


    @Test
    void removeAppUser_caretakerShouldBeCascadeDeleted() {
        appUserRepository.deleteAll();
        appUserRepository.flush();

        assertEquals(0, caretakerRepository.count());
    }

    @Test
    void removeCaretaker_appUserShouldNotBeDeleted() {
        caretakerRepository.deleteAll();
        caretakerRepository.flush();

        assertEquals(1, appUserRepository.count());
    }
}
