package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
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
        MockOfferProvider.editComplexMockOfferForCaretaker(caretaker);
        MockOfferProvider.addMockAvailabilitiesToOffer(caretaker.getOffers().get(0));

        setIds(caretaker);
        setCalculatedFields(caretaker);

        CaretakerComplexInfoDTO caretakerComplexInfoDTO = mapper.mapToCaretakerComplexInfoDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerComplexInfoDTO));
    }

    private void setIds(Caretaker caretaker) {
        caretaker.getAddress().setId(1L);
        caretaker.getOffers().forEach(offer -> {
            offer.getAnimalAmenities().forEach(amenity -> amenity.setId(1L));
            offer.getOfferConfigurations().forEach(offerConfiguration -> {
                offerConfiguration.setId(1L);
                offerConfiguration.getOfferOptions().forEach(offerOption -> offerOption.setId(1L));
            });
            offer.getAvailabilities().forEach(availability -> availability.setId(1L));
        });
    }

    private void setCalculatedFields(Caretaker caretaker) {
        caretaker.setAvgRating(4.5f);
        caretaker.setNumberOfRatings(2);
    }

}
