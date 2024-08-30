package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockOfferProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaretakerMapperTest {

    private final CaretakerMapper mapper = CaretakerMapper.INSTANCE;


    @Test
    void mapToCaretakerDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        MockOfferProvider.createComplexMockOfferForCaretaker(caretaker);

        setIds(caretaker);

        CaretakerDTO caretakerDTO = mapper.mapToCaretakerDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerDTO));
    }

    private void setIds(Caretaker caretaker) {
        caretaker.getAddress().setId(1L);
        caretaker.getOffers().forEach(offer -> {
            offer.getAnimalAmenities().forEach(amenity -> amenity.setId(1L));
            offer.getOfferConfigurations().forEach(offerConfiguration -> {
                offerConfiguration.setId(1L);
                offerConfiguration.getOfferOptions().forEach(offerOption -> offerOption.setId(1L));
            });
        });
    }

}
