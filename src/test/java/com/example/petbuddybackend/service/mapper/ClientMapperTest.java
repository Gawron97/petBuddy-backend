package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientMapperTest {

    private final ClientMapper mapper = ClientMapper.INSTANCE;

    @Test
    void mapToClientDTO_shouldNotLeaveNullFields() {
        Client client = MockUserProvider.createMockClient();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();
        client.getAccountData().setProfilePicture(profilePicture);

        ClientDTO mappingResult = mapper.mapToClientDTO(client);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappingResult));
    }
}
