package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.block.BlockRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import com.example.petbuddybackend.utils.exception.throweable.InvalidRoleException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

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

    @Mock
    private BlockRepository blockRepository;

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
        when(photoService.findPhotoLinkByNullableId(existingProfilePicture.getBlob())).thenReturn(Optional.of(existingProfilePicture));

        // When
        UserProfilesData result = userService.getProfileData(EMAIL);

        // Then
        verify(photoService).updatePhotoExpiration(existingProfilePicture);
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
        UserProfilesData result = userService.getProfileData(EMAIL);

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
        userService.uploadProfilePicture(USERNAME, profilePicture);

        // Then
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertEquals(photoLink, savedUser.getProfilePicture());
    }

    @Test
    void uploadProfilePicture_userAlreadyHadProfilePicture_oldProfilePictureIsRemoved() {
        // Given
        AppUser user = AppUser.builder()
                .email(USERNAME)
                .profilePicture(photoLink)
                .build();

        PhotoLink newPhoto = MockUserProvider.createMockPhotoLink();

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(user));
        when(photoService.uploadPhoto(profilePicture)).thenReturn(newPhoto);
        when(photoService.findPhotoLinkByNullableId(photoLink.getBlob())).thenReturn(Optional.of(photoLink));


        // When
        userService.uploadProfilePicture(USERNAME, profilePicture);

        // Then
        verify(userRepository).save(userCaptor.capture());
        verify(photoService).deletePhoto(photoLink);
        AppUser savedUser = userCaptor.getValue();
        assertEquals(newPhoto, savedUser.getProfilePicture());
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
        when(photoService.findPhotoLinkByNullableId(photoLink.getBlob())).thenReturn(Optional.of(photoLink));

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

    @Test
    void blockUser_whenUserBlocksAnotherUser_shouldSucceed() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(false); // User not already blocked

        // When
        userService.blockUser(blockerUsername, blockedUsername);

        // Then
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void blockUser_whenUserBlocksThemselves_shouldThrowException() {
        // Given
        String username = "same@example.com";

        // When & Then
        IllegalActionException exception = assertThrows(IllegalActionException.class, () ->
                userService.blockUser(username, username));
        assertEquals("User cannot block himself", exception.getMessage());
    }

    @Test
    void blockUser_whenUserIsAlreadyBlocked_shouldThrowException() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(true); // User already blocked

        // When & Then
        assertThrows(AlreadyBlockedException.class, () ->
                userService.blockUser(blockerUsername, blockedUsername));
    }

    @Test
    void unblockUser_whenBlockExists_shouldSucceed() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.findById(any(BlockId.class))).thenReturn(Optional.of(new Block(blockerUsername, blockedUsername)));

        // When
        userService.unblockUser(blockerUsername, blockedUsername);

        // Then
        verify(blockRepository).delete(any(Block.class));
    }

    @Test
    void unblockUser_whenBlockDoesNotExist_shouldThrowException() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.findById(any(BlockId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                userService.unblockUser(blockerUsername, blockedUsername));
    }

    @Test
    void unblockUser_whenUserUnblocksThemselves_shouldThrowException() {
        // Given
        String username = "same@example.com";

        // When & Then
        assertThrows(IllegalActionException.class, () ->
                userService.unblockUser(username, username));
    }

    @Test
    void isBlocked_whenUserIsBlocked_shouldReturnTrue() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(true);

        // When
        boolean result = userService.isBlocked(blockerUsername, blockedUsername);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlocked_whenUserIsNotBlocked_shouldReturnFalse() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(false);

        // When
        boolean result = userService.isBlocked(blockerUsername, blockedUsername);

        // Then
        assertFalse(result);
    }

    @Test
    void assertNotBlockedByAny_whenBlockedByOtherUser_shouldThrowException() {
        // Given
        String firstUsername = "user1@example.com";
        String secondUsername = "user2@example.com";

        when(blockRepository.existsById(eq(new BlockId(firstUsername, secondUsername)))).thenReturn(true);

        // When & Then
        assertThrows(BlockedException.class, () ->
                userService.assertNotBlockedByAny(firstUsername, secondUsername));
    }

    @Test
    void assertNotBlockedByAny_whenBlockedByOtherUserReversed_shouldThrowException() {
        // Given
        String firstUsername = "user1@example.com";
        String secondUsername = "user2@example.com";

        when(blockRepository.existsById(eq(new BlockId(secondUsername, firstUsername)))).thenReturn(true);

        // When & Then
        assertThrows(BlockedException.class, () ->
                userService.assertNotBlockedByAny(firstUsername, secondUsername));
    }

    @Test
    void assertHasRole_whenUserIsClient_shouldSucceed() {
        // Given
        String clientEmail = "client@example.com";

        when(clientRepository.existsById(clientEmail)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> userService.assertHasRole(clientEmail, Role.CLIENT));
    }

    @Test
    void assertHasRole_whenUserIsCaretaker_shouldSucceed() {
        // Given
        String clientEmail = "caretaker@example.com";

        when(caretakerRepository.existsById(clientEmail)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> userService.assertHasRole(clientEmail, Role.CARETAKER));
    }

    @Test
    void assertHasRole_whenUserIsNotClient_shouldThrowInvalidRoleException() {
        // Given
        String caretakerEmail = "caretaker@example.com";

        when(caretakerRepository.existsById(caretakerEmail)).thenReturn(true);

        // When & Then
        assertThrows(InvalidRoleException.class, () -> userService.assertHasRole(caretakerEmail, Role.CLIENT));
    }
}
