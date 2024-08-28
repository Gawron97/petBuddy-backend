package com.example.petbuddybackend.service.offer;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.testutils.mock.MockAnimalProvider;
import com.example.petbuddybackend.testutils.mock.MockOfferProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferServiceUnitTest {


    @Mock
    private OfferRepository offerRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private CaretakerService caretakerService;

    @InjectMocks
    private OfferService offerService;

    private Caretaker caretaker;
    private Animal animalInExistingOffer;
    private Offer existingOffer;

    @BeforeEach
    void setUp() {

        caretaker = MockUserProvider.createMockCaretaker();
        animalInExistingOffer = MockAnimalProvider.createMockAnimal("DOG");
        existingOffer = MockOfferProvider.createMockOffer(caretaker, animalInExistingOffer);

    }

    @Test
    void addOrEditOffer_WhenOfferDoesNotExists_ShouldCreateNewOffer() {

        // Given
        OfferDTO offerToCreate = OfferDTO.builder()
                .description("description")
                .animal(AnimalDTO.builder().animalType("DOG").build())
                .build();

        when(offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(
                caretaker.getEmail(), offerToCreate.animal().animalType())).thenReturn(Optional.empty());
        when(animalRepository.findById("DOG")).thenReturn(Optional.of(animalInExistingOffer));
        when(caretakerService.getCaretakerByEmail(caretaker.getEmail())).thenReturn(caretaker);

        // When
        offerService.addOrEditOffer(offerToCreate, caretaker.getEmail());

        // Then

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository, times(1)).save(offerCaptor.capture());
        Offer captoredOffer = offerCaptor.getValue();

        assertNotNull(captoredOffer);
        assertEquals(offerToCreate.description(), captoredOffer.getDescription());
        assertNull(captoredOffer.getOfferConfigurations());
        assertNull(captoredOffer.getAnimalAmenities());

    }


}
