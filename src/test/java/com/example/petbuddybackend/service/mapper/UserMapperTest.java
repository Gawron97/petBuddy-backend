package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ProfileData;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserMapperTest {

    private final UserMapper mapper = UserMapper.INSTANCE;

    @Test
    void mapToProfileData_shouldNotLeaveNullFields() {
        AppUser user = MockUserProvider.createMockAppUser(MockUserProvider.createMockPhotoLink());

        ProfileData profileData = mapper.mapToProfileData(user, true, true);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(profileData));
    }
}
