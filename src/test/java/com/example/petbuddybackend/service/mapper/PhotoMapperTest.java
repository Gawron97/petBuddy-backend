package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhotoMapperTest {

    private final PhotoMapper mapper = PhotoMapper.INSTANCE;

    @Test
    void mapToPhotoLinkDTO_shouldNotLeaveNullFields() {
        PhotoLink photoLink = MockUserProvider.createMockPhotoLink();

        assertTrue(ValidationUtils.fieldsNotNullRecursive(mapper.mapToPhotoLinkDTO(photoLink)));
    }
}
