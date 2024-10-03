package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.GeneralMockProvider.createJwtToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    public static final String USERNAME = "testuser";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String EMAIL = "test@example.com";

    private static final MultipartFile profilePicture = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            new byte[]{1, 2, 3}
    );

    private static final PhotoLink photoLink = new PhotoLink(
            "testBlob",
            "testURL",
            LocalDateTime.now()
    );

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CaretakerRepository caretakerRepository;

    @Mock
    private PhotoService photoService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<AppUser> userCaptor;

    private AutoCloseable closeable;


    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void givenUserDoesNotExist_whenCreateUserIfNotExist_thenUserIsCreated() {
        // given
        JwtAuthenticationToken token = createJwtToken(EMAIL, NAME, SURNAME, USERNAME);

        when(userRepository.findById(EMAIL)).thenReturn(Optional.empty());

        //when
        userService.createUserIfNotExistOrGet(token);

        //then
        verify(userRepository).save(userCaptor.capture());
        AppUser user = userCaptor.getValue();
        assertEquals(EMAIL, user.getEmail());
        assertEquals(NAME, user.getName());
        assertEquals(SURNAME, user.getSurname());
    }

    @Test
    void givenUserAlreadyExists_whenCreateUserIfNotExist_thenUserIsNotCreated() {
        // given
        JwtAuthenticationToken token = createJwtToken(EMAIL, NAME, SURNAME, USERNAME);

        when(userRepository.existsById(EMAIL)).thenReturn(true);
        when(userRepository.findById(EMAIL)).thenReturn(Optional.of(new AppUser()));

        //when
        userService.createUserIfNotExistOrGet(token);

        //then
        verify(userRepository, never()).save(any(AppUser.class));

    }

    @ParameterizedTest
    @MethodSource("userProfilesProvider")
    void getUserProfiles(boolean clientExists, boolean caretakerExists, UserProfiles expectedProfiles) {
        //Given
        when(userRepository.existsById(EMAIL)).thenReturn(true);
        when(clientRepository.existsById(EMAIL)).thenReturn(clientExists);
        when(caretakerRepository.existsById(EMAIL)).thenReturn(caretakerExists);

        //When
        UserProfiles result = userService.getUserProfiles(EMAIL);

        //Then
        assertEquals(expectedProfiles, result);

    }

    @Test
    void getProfileData_shouldSucceed() {
        // Given
        PhotoLink existingProfilePicture = new PhotoLink("oldBlob", "oldURL", LocalDateTime.now());
        AppUser userWithPicture = AppUser.builder()
                .email(EMAIL)
                .profilePicture(existingProfilePicture)
                .build();

        when(userRepository.findById(EMAIL)).thenReturn(Optional.of(userWithPicture));
        when(clientRepository.existsById(EMAIL)).thenReturn(true);
        when(caretakerRepository.existsById(EMAIL)).thenReturn(false);
        when(photoService.findByNullableId(existingProfilePicture.getBlob())).thenReturn(Optional.of(existingProfilePicture));

        // When
        ProfileData result = userService.getProfileData(EMAIL);

        // Then
        verify(photoService).findByNullableId(existingProfilePicture.getBlob());
        assertEquals(EMAIL, result.accountData().email());
        assertEquals(existingProfilePicture.getBlob(), result.accountData().profilePicture().blob());
    }

    @Test
    void getRenewedProfileData_userHasNoProfilePicture_shouldNotUpdatePhotoExpiration() {
        // Given
        AppUser userWithoutPicture = AppUser.builder()
                .email(EMAIL)
                .build();

        when(userRepository.findById(EMAIL)).thenReturn(Optional.of(userWithoutPicture));
        when(clientRepository.existsById(EMAIL)).thenReturn(true);
        when(caretakerRepository.existsById(EMAIL)).thenReturn(true);

        // When
        ProfileData result = userService.getProfileData(EMAIL);

        // Then
        verify(photoService, never()).updatePhotoExpiration(any(PhotoLink.class));
        assertNull(result.accountData().profilePicture());
    }

    @Test
    void getRenewedProfileData_userDoesNotExist_shouldThrowNotFoundException() {
        // Given
        when(userRepository.findById(EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.getProfileData(EMAIL));
        verify(photoService, never()).updatePhotoExpiration(any(PhotoLink.class));
    }

    @Test
    void uploadProfilePicture_whenUserExists_thenProfilePictureIsUploaded() {
        // Given
        AppUser user = AppUser.builder()
                .email(USERNAME)
                .build();

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(user));
        when(photoService.uploadPhoto(profilePicture)).thenReturn(photoLink);

        // When
        PhotoLinkDTO result = userService.uploadProfilePicture(USERNAME, profilePicture);

        // Then
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertEquals(photoLink, savedUser.getProfilePicture());
        assertEquals(photoLink.getUrl(), result.url());
        assertEquals(photoLink.getBlob(), result.blob());
    }

    @Test
    void uploadProfilePicture_userAlreadyHadProfilePicture_oldProfilePictureIsRemoved() {
        // Given
        AppUser user = AppUser.builder()
                .email(USERNAME)
                .profilePicture(photoLink)
                .build();

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(user));
        when(photoService.uploadPhoto(profilePicture)).thenReturn(photoLink);
        when(photoService.findByNullableId(photoLink.getBlob())).thenReturn(Optional.of(photoLink));


        // When
        PhotoLinkDTO result = userService.uploadProfilePicture(USERNAME, profilePicture);

        // Then
        verify(userRepository).save(userCaptor.capture());
        verify(photoService).deletePhoto(photoLink);
        AppUser savedUser = userCaptor.getValue();
        assertEquals(photoLink, savedUser.getProfilePicture());
        assertEquals(photoLink.getUrl(), result.url());
        assertEquals(photoLink.getBlob(), result.blob());
    }

    @Test
    void uploadProfilePicture_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.uploadProfilePicture(EMAIL, profilePicture));
        verify(photoService, never()).uploadPhoto(profilePicture);
    }

    @Test
    void deleteProfilePicture_whenUserExistsAndProfilePicturePresent_thenProfilePictureIsDeleted() {
        // Given
        AppUser user = new AppUser();
        user.setProfilePicture(photoLink);

        when(userRepository.findById(EMAIL)).thenReturn(Optional.of(user));
        when(photoService.findByNullableId(photoLink.getBlob())).thenReturn(Optional.of(photoLink));

        // When
        userService.deleteProfilePicture(EMAIL);

        // Then
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertNull(savedUser.getProfilePicture());
        verify(photoService).deletePhoto(photoLink);
    }

    @Test
    void deleteProfilePicture_whenUserExistsAndProfilePictureIsNull_thenDoNothing() {
        // Given
        AppUser user = new AppUser();
        user.setProfilePicture(null);

        when(userRepository.findById(EMAIL)).thenReturn(Optional.of(user));

        // When
        userService.deleteProfilePicture(EMAIL);

        // Then
        verify(userRepository, never()).save(any(AppUser.class));
        verify(photoService, never()).deletePhoto(any(String.class));
    }

    @Test
    void deleteProfilePicture_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.deleteProfilePicture(EMAIL));
        verify(photoService, never()).deletePhoto(any(String.class));
    }

    private static Stream<Arguments> userProfilesProvider() {
        return Stream.of(
                Arguments.of(
                        true,
                        true,
                        UserProfiles.builder()
                                .email(EMAIL)
                                .hasClientProfile(true)
                                .hasCaretakerProfile(true)
                                .build()
                ),
                Arguments.of(
                        false,
                        false,
                        UserProfiles.builder()
                                .email(EMAIL)
                                .hasClientProfile(false)
                                .hasCaretakerProfile(false)
                                .build()
                ),
                Arguments.of(
                        false,
                        true,
                        UserProfiles.builder()
                                .email(EMAIL)
                                .hasClientProfile(false)
                                .hasCaretakerProfile(true)
                                .build()
                ),
                Arguments.of(
                        true,
                        false,
                        UserProfiles.builder()
                                .email(EMAIL)
                                .hasClientProfile(true)
                                .hasCaretakerProfile(false)
                                .build()
                )
        );
    }

    @Test
    void getUserProfiles_whenUserNotExists_thenThrowNotFoundException() {
        //Given
        when(userRepository.existsById(EMAIL)).thenReturn(false);

        //When Then
        assertThrows(NotFoundException.class, () -> userService.getUserProfiles(EMAIL));
    }
}
