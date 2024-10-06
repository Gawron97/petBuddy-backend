package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserMapperTest {

    private final UserMapper mapper = UserMapper.INSTANCE;

    @Test
    void mapToProfileData_shouldNotLeaveNullFields() {
        PhotoLink photo = MockUserProvider.createMockPhotoLink();
        AppUser user = MockUserProvider.createMockAppUser(photo);

        UserProfilesData profileData = mapper.mapToProfileData(user, true, true);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(profileData));
    }

    @Test
    void mapToAccountDataDTO_shouldNotLeaveNullFields() {
        PhotoLink photo = MockUserProvider.createMockPhotoLink();
        AppUser user = MockUserProvider.createMockAppUser(photo);

        AccountDataDTO accountDataDTO = mapper.mapToAccountDataDTO(user);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(accountDataDTO));
    }
}
