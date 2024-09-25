package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.GeneralMockProvider.createJwtToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CaretakerRepository caretakerRepository;

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

        when(userRepository.existsById(email)).thenReturn(true);
        when(userRepository.findById(email)).thenReturn(Optional.of(new AppUser()));

        //when
        userService.createUserIfNotExistOrGet(token);

        //then
        verify(userRepository, never()).save(any(AppUser.class));

    }

    @ParameterizedTest
    @MethodSource("userProfilesProvider")
    void getUserProfiles(boolean clientExists, boolean caretakerExists, UserProfiles expectedProfiles) {

        //Given
        String email = "test@example.com";

        when(userRepository.existsById(email)).thenReturn(true);
        when(clientRepository.existsById(email)).thenReturn(clientExists);
        when(caretakerRepository.existsById(email)).thenReturn(caretakerExists);

        //When
        UserProfiles result = userService.getUserProfiles(email);

        //Then
        assertEquals(expectedProfiles, result);

    }

    private static Stream<Arguments> userProfilesProvider() {
        return Stream.of(
                Arguments.of(
                        true,
                        true,
                        UserProfiles.builder()
                                .email("test@example.com")
                                .hasClientProfile(true)
                                .hasCaretakerProfile(true)
                                .build()
                ),
                Arguments.of(
                        false,
                        false,
                        UserProfiles.builder()
                                .email("test@example.com")
                                .hasClientProfile(false)
                                .hasCaretakerProfile(false)
                                .build()
                ),
                Arguments.of(
                        false,
                        true,
                        UserProfiles.builder()
                                .email("test@example.com")
                                .hasClientProfile(false)
                                .hasCaretakerProfile(true)
                                .build()
                ),
                Arguments.of(
                        true,
                        false,
                        UserProfiles.builder()
                                .email("test@example.com")
                                .hasClientProfile(true)
                                .hasCaretakerProfile(false)
                                .build()
                )
        );
    }

    @Test
    void getUserProfiles_whenUserNotExists_thenThrowNotFoundException() {

        //Given
        String email = "test@example.com";

        when(userRepository.existsById(email)).thenReturn(false);

        //When Then
        assertThrows(NotFoundException.class, () -> userService.getUserProfiles(email));

    }

}
