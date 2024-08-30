package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static com.example.petbuddybackend.testutils.mock.GeneralMockProvider.createJwtToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<AppUser> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenUserDoesNotExist_whenCreateUserIfNotExist_thenUserIsCreated() {
        // given
        String email = "test@example.com";
        String firstname = "name";
        String lastname = "surname";
        String username = "testuser";

        JwtAuthenticationToken token = createJwtToken(email, firstname, lastname, username);

        when(userRepository.findById(email)).thenReturn(Optional.empty());

        //when
        userService.createUserIfNotExistOrGet(token);

        //then
        verify(userRepository).save(userCaptor.capture());
        AppUser user = userCaptor.getValue();
        assertEquals(email, user.getEmail());
        assertEquals(firstname, user.getName());
        assertEquals(lastname, user.getSurname());
    }

    @Test
    void givenUserAlreadyExists_whenCreateUserIfNotExist_thenUserIsNotCreated() {
        // given
        String email = "test@example.com";
        String firstname = "name";
        String lastname = "surname";
        String username = "testuser";

        JwtAuthenticationToken token = createJwtToken(email, firstname, lastname, username);

        when(userRepository.findById(email)).thenReturn(Optional.of(new AppUser()));

        //when
        userService.createUserIfNotExistOrGet(token);

        //then
        verify(userRepository, never()).save(any(AppUser.class));

    }

}
