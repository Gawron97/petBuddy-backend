package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.block.BlockRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.UserMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String USER = "User";
    private static final String USER_CANNOT_BLOCK_HIMSELF_MESSAGE = "User cannot block himself";
    private static final String USER_ALREADY_BLOCKED_MESSAGE = "User %s is already blocked";
    private static final String USER_NOT_BLOCKED_MESSAGE = "User %s was not blocked in the first place or does not exist";

    private final AppUserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CaretakerRepository caretakerRepository;
    private final BlockRepository blockRepository;
    private final PhotoService photoService;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    @Transactional
    public AppUser createUserIfNotExistOrGet(JwtAuthenticationToken token) {

        String email = (String) token.getTokenAttributes().get("email");

        if(!userRepository.existsById(email)) {
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
                .orElseThrow(() -> NotFoundException.withFormattedMessage(USER, email));
    }

    /**
     * @return {@link UserProfilesData} with updated {@link PhotoLink} url
     * */
    public UserProfilesData getProfileData(String email) {
        AppUser user = getAppUser(email);
        renewProfilePicture(user);

        return userMapper.mapToProfileData(
                user,
                clientRepository.existsById(email),
                caretakerRepository.existsById(email)
        );
    }

    @Transactional
    public AccountDataDTO uploadProfilePicture(String username, MultipartFile profilePicture) {
        AppUser user = getAppUser(username);
        PhotoLink oldPhoto = user.getProfilePicture();

        if(oldPhoto != null) {
            photoService.deletePhoto(oldPhoto);
        }

        PhotoLink newPhoto = photoService.uploadPhoto(profilePicture);
        user.setProfilePicture(newPhoto);
        return userMapper.mapToAccountDataDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteProfilePicture(String username) {
        AppUser user = getAppUser(username);
        PhotoLink profilePicture = user.getProfilePicture();

        if(profilePicture == null) {
            return;
        }

        photoService.deletePhoto(profilePicture);
        user.setProfilePicture(null);
        userRepository.save(user);
    }

    public void renewProfilePicture(AppUser user) {
        PhotoLink photoLink = user.getProfilePicture();

        if(photoLink != null) {
            photoService.updatePhotoExpiration(photoLink);
        }
    }

    public void blockUser(String blockerUsername, String blockedUsername) {
        assertDoesNotBlockSelf(blockerUsername, blockedUsername);
        assertIsNotBlocked(blockerUsername, blockedUsername);

        Block block = new Block(blockerUsername, blockedUsername);
        blockRepository.save(block);
    }

    public void unblockUser(String blockerUsername, String blockedUsername) {
        assertDoesNotBlockSelf(blockerUsername, blockedUsername);

        Block block = getBlock(blockerUsername, blockedUsername);
        blockRepository.delete(block);
    }

    public Block getBlock(String blockerUsername, String blockedUsername) {
        return blockRepository.findById(new BlockId(blockerUsername, blockedUsername))
                .orElseThrow(() -> new NotFoundException(String.format(USER_NOT_BLOCKED_MESSAGE, blockedUsername)));
    }

    public boolean isBlocked(String blockerUsername, String blockedUsername) {
        return blockRepository.existsById(new BlockId(blockerUsername, blockedUsername));
    }

    private void assertIsNotBlocked(String blockerUsername, String blockedUsername) {
        if(isBlocked(blockerUsername, blockedUsername)) {
            throw new AlreadyBlockedException(USER_ALREADY_BLOCKED_MESSAGE);
        }
    }

    private void assertDoesNotBlockSelf(String blockerUsername, String blockedUsername) {
        if(blockerUsername.equals(blockedUsername)) {
            throw new IllegalActionException(USER_CANNOT_BLOCK_HIMSELF_MESSAGE);
        }
    }
}
