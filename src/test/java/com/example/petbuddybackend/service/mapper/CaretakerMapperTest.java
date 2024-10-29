package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexPublicDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockOfferProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CaretakerMapperTest {

    private final CaretakerMapper mapper = CaretakerMapper.INSTANCE;

    @Test
    void mapToCaretakerComplexDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();
        caretaker.getAccountData().setProfilePicture(profilePicture);
        MockOfferProvider.editComplexMockOfferForCaretaker(caretaker);
        MockOfferProvider.setMockAvailabilitiesToOffer(caretaker.getOffers().get(0));

        setIds(caretaker);
        setCalculatedFields(caretaker);

        CaretakerComplexDTO caretakerComplexDTO = mapper.mapToCaretakerComplexDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerComplexDTO));
    }

    @Test
    void mapToCaretakerPublicComplexDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();
        caretaker.getAccountData().setProfilePicture(profilePicture);
        MockOfferProvider.editComplexMockOfferForCaretaker(caretaker);
        MockOfferProvider.setMockAvailabilitiesToOffer(caretaker.getOffers().get(0));

        setIds(caretaker);
        setCalculatedFields(caretaker);

        CaretakerComplexPublicDTO caretakerPublicComplexDTO = mapper.mapToCaretakerComplexPublicDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerPublicComplexDTO));
    }

    @Test
    void mapToCaretaker_shouldNotLeaveNullFields() {
        AppUser accountData = MockUserProvider.createMockAppUser();
        AddressDTO addressDTO = AddressMapper.INSTANCE.mapToAddressDTO(MockUserProvider.createMockAddress());

        ModifyCaretakerDTO dto = ModifyCaretakerDTO.builder()
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
        caretaker.getAccountData().setProfilePicture(profilePicture);

        // Set calculated fields to pass the test
        caretaker.setNumberOfRatings(1);
        caretaker.setAvgRating(4.5f);

        CaretakerDTO caretakerMappingResult = mapper.mapToCaretakerDTO(caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretakerMappingResult));
    }

    @Test
    void updateCaretakerFromDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        PhotoLink profilePicture = MockUserProvider.createMockPhotoLink();
        caretaker.getAccountData().setProfilePicture(profilePicture);
        caretaker.setNumberOfRatings(1);
        caretaker.setAvgRating(4.5f);

        ModifyCaretakerDTO dto = ModifyCaretakerDTO.builder()
                        .phoneNumber("12345678")
                        .description("description")
                        .address(AddressMapper.INSTANCE.mapToAddressDTO(MockUserProvider.createMockAddress()))
                        .build();

        mapper.updateCaretakerFromDTO(dto, caretaker);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(caretaker));
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
