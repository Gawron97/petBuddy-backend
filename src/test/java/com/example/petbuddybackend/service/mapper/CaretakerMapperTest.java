package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockOfferProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaretakerMapperTest {

    private final CaretakerMapper mapper = CaretakerMapper.INSTANCE;


    @Test
    void mapToCaretakerComplexInfoDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();
        MockOfferProvider.editComplexMockOfferForCaretaker(caretaker);
        MockOfferProvider.setMockAvailabilitiesToOffer(caretaker.getOffers().get(0));

        setIds(caretaker);
        setCalculatedFields(caretaker);

        CaretakerComplexInfoDTO caretakerComplexInfoDTO = mapper.mapToCaretakerComplexInfoDTO(caretaker, profilePicture);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerComplexInfoDTO));
    }

    @Test
    void mapToCaretaker_shouldNotLeaveNullFields() {
        AppUser accountData = MockUserProvider.createMockAppUser();
        AddressDTO addressDTO = AddressMapper.INSTANCE.mapToAddressDTO(MockUserProvider.createMockAddress());

        CreateCaretakerDTO dto = CreateCaretakerDTO.builder()
                        .phoneNumber("12345678")
                        .description("description")
                        .address(addressDTO)
                        .build();

        Caretaker caretakerMappingResult = mapper.mapToCaretaker(dto, accountData);

        // Set calculated fields to pass the test
        caretakerMappingResult.setAvgRating(4.5f);
        caretakerMappingResult.setNumberOfRatings(2);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerMappingResult));
    }

    @Test
    void mapToCaretakerDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();

        // Set calculated fields to pass the test
        caretaker.setNumberOfRatings(1);
        caretaker.setAvgRating(4.5f);

        CaretakerDTO caretakerMappingResult = mapper.mapToCaretakerDTO(caretaker, profilePicture);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerMappingResult));
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
