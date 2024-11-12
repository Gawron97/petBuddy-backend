package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.photo.PhotoLimitException;
import com.example.petbuddybackend.utils.provider.geolocation.GeolocationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCoordinates;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CaretakerServiceTest {

    @Autowired
    private CaretakerService caretakerService;

    @MockBean
    private CaretakerRepository caretakerRepository;

    @MockBean
    private PhotoService photoService;

    @MockBean
    private GeolocationProvider geolocationProvider;

    @Test
    void testPutOfferPhotos_caretakerDoesNotExist_shouldThrowNotFoundException() {
        // Given
        String notCaretakerEmail = "not a caretaker";

        // When
        when(caretakerRepository.findById(notCaretakerEmail))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class,
                () -> caretakerService.putOfferPhotos(notCaretakerEmail, Collections.emptySet(), Collections.emptyList()));
    }

    @Test
    void testPutOfferPhotos_shouldSucceed() {
        // Given
        String blob1 = "blob1";
        String blob2 = "blob2";
        String newBlob = "newBlob";
        MockMultipartFile photo = new MockMultipartFile("file", "", "text/plain", new byte[]{0});
        PhotoLink photoToReturn = MockUserProvider.createMockPhotoLink(newBlob);
        PhotoLink photo1 = MockUserProvider.createMockPhotoLink(blob1);
        PhotoLink photo2 = MockUserProvider.createMockPhotoLink(blob2);
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        caretaker.setOfferPhotos(new ArrayList<>(List.of(photo1, photo2)));

        Caretaker caretakerWithPhoto = MockUserProvider.createMockCaretaker();
        caretakerWithPhoto.setOfferPhotos(new ArrayList<>(List.of(photo1, photoToReturn)));

        // When
        when(caretakerRepository.findById(eq(caretaker.getEmail())))
                .thenReturn(Optional.of(caretaker));

        when(photoService.uploadPhotos(eq(List.of(photo))))
                .thenReturn(List.of(photoToReturn));

        when(caretakerRepository.save(any()))
                .thenReturn(caretakerWithPhoto);

        List<PhotoLinkDTO> patchedPhotos =
                caretakerService.putOfferPhotos(caretaker.getEmail(), Set.of(blob1), List.of(photo));

        // Then
        assertFalse(patchedPhotos.isEmpty());
        assertEquals(2, patchedPhotos.size());
        assertEquals(blob1, patchedPhotos.get(0).blob());
        assertEquals(newBlob, patchedPhotos.get(1).blob());
    }

    @ParameterizedTest
    @MethodSource("providePhotosAndBlobsExceedingLimit")
    void testEditCaretaker_offerPhotoCountExceedsLimit_shouldThrow(
            Set<String> offerBlobsToKeep,
            List<MultipartFile> newPhotos
    ) {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        ModifyCaretakerDTO dto = ModifyCaretakerDTO.builder()
                .address(
                        AddressDTO.builder()
                        .city("city")
                        .street("street")
                        .build()
                )
                .build();
        List<PhotoLink> photosMachingBlobs = offerBlobsToKeep.stream()
                .map(blob -> PhotoLink.builder().blob(blob).build())
                .toList();

        caretaker.setOfferPhotos(new ArrayList<>(photosMachingBlobs));

        when(caretakerRepository.findById(eq(caretaker.getEmail())))
                .thenReturn(Optional.of(caretaker));
        when(geolocationProvider.getCoordinatesOfAddress(anyString(), anyString(), anyString()))
                .thenReturn(createMockCoordinates());

        assertThrows(PhotoLimitException.class,
                () -> caretakerService.editCaretaker(dto, caretaker.getEmail(), offerBlobsToKeep, newPhotos));
    }

    @Test
    void testAddCaretaker_offerPhotoCountExceedsLimit_shouldThrow() {
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        ModifyCaretakerDTO dto = ModifyCaretakerDTO.builder().build();
        List<MultipartFile> newPhotos = generateEmptyMultipartFiles(11);

        when(caretakerRepository.findById(eq(caretaker.getEmail())))
                .thenReturn(Optional.of(caretaker));

        assertThrows(PhotoLimitException.class,
                () -> caretakerService.addCaretaker(dto, caretaker.getEmail(), newPhotos));
    }

    private static Stream<Arguments> providePhotosAndBlobsExceedingLimit() {
        return Stream.of(
                Arguments.of(
                        generateStrings(11),
                        Collections.emptyList()
                ),
                Arguments.of(
                        Collections.emptySet(),
                        generateEmptyMultipartFiles(11)
                ),
                Arguments.of(
                        generateStrings(6),
                        generateEmptyMultipartFiles(5)
                ),
                Arguments.of(
                        generateStrings(5),
                        generateEmptyMultipartFiles(6)
                )
        );
    }

    private static Set<String> generateStrings(int count) {
        return Stream.generate(() -> UUID.randomUUID().toString())
                .limit(count)
                .collect(Collectors.toSet());
    }

    private static List<MultipartFile> generateEmptyMultipartFiles(int count) {
        List<MultipartFile> listToReturn = new ArrayList<>(count);

        for(int i=0; i<count; i++) {
            listToReturn.add(
                    new MockMultipartFile(
                            UUID.randomUUID().toString(),
                            new byte[]{1,2,3}
                    )
            );
        }

        return listToReturn;
    }
}