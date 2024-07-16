package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.testutils.MockUtils;
import com.example.petbuddybackend.testutils.ValidationUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaretakerMapperTest {

    private final CaretakerMapper mapper = CaretakerMapper.INSTANCE;


    @Test
    void mapToCaretakerDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        var caretaker = MockUtils.createMockCaretaker();
        caretaker.getAddress().setId(1L);

        var caretakerDTO = mapper.mapToCaretakerDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNull(caretakerDTO));
        assertTrue(ValidationUtils.fieldsNotNull(caretakerDTO.address()));
    }
}
