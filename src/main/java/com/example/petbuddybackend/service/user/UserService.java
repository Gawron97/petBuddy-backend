package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.UserMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.service.mapper.PhotoMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String USER = "User";

    private final AppUserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CaretakerRepository caretakerRepository;
    private final PhotoService photoService;
    private final PhotoMapper photoMapper = PhotoMapper.INSTANCE;
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

    public UserProfiles getUserProfiles(String email) {
        assertUserExists(email);
        return UserProfiles.builder()
                .email(email)
                .hasClientProfile(clientRepository.existsById(email))
                .hasCaretakerProfile(caretakerRepository.existsById(email))
                .build();
    }

    /**
     * @return {@link ProfileData} with updated {@link PhotoLink} url
     * */
    public ProfileData getProfileData(String email) {
        AppUser user = getAppUser(email);
        Optional<PhotoLink> profilePicture = photoService.findByNullableId(user.getProfilePictureBlob());

        return userMapper.mapToProfileData(
                user,
                profilePicture.orElse(null),
                clientRepository.existsById(email),
                caretakerRepository.existsById(email)
        );
    }

    @Transactional
    public PhotoLinkDTO uploadProfilePicture(String username, MultipartFile profilePicture) {
        AppUser user = getAppUser(username);
        Optional<PhotoLink> oldPhoto = photoService.findByNullableId(user.getProfilePictureBlob());

        oldPhoto.ifPresent(photoService::deletePhoto);

        PhotoLink newPhoto = photoService.uploadPhoto(profilePicture);
        user.setProfilePictureBlob(newPhoto.getBlob());
        userRepository.save(user);

        return photoMapper.mapToPhotoLinkDTO(newPhoto);
    }

    @Transactional
    public void deleteProfilePicture(String username) {
        AppUser user = getAppUser(username);
        Optional<PhotoLink> profilePicture = photoService.findByNullableId(user.getProfilePictureBlob());

        if(profilePicture.isEmpty()) {
            return;
        }

        user.setProfilePictureBlob(null);
        userRepository.save(user);
        photoService.deletePhoto(profilePicture.get());
    }

    private void assertUserExists(String email) {
        if(!userRepository.existsById(email)) {
            throw NotFoundException.withFormattedMessage(USER, email);
        }
    }
}
