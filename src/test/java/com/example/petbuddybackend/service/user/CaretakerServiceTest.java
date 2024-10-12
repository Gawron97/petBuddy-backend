package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CaretakerServiceTest {

    @Autowired
    private CaretakerService caretakerService;

    @MockBean
    private CaretakerRepository caretakerRepository;

    @MockBean
    private PhotoService photoService;

    @Test
    void testPatchOfferPhotos_caretakerDoesNotExist_shouldThrowNotFoundException() {
        // Given
        String notCaretakerEmail = "not a caretaker";

        // When
        when(caretakerRepository.findById(notCaretakerEmail))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class,
                () -> caretakerService.patchOfferPhotos(notCaretakerEmail, Collections.emptySet(), Collections.emptyList()));
    }

    @Test
    void testPatchOfferPhotos_shouldSucceed() {
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
        when(photoService.uploadPhotos(eq(List.of(photo))))
                .thenReturn(List.of(photoToReturn));

        when(caretakerRepository.findById(eq(caretaker.getEmail())))
                .thenReturn(Optional.of(caretaker));

        when(caretakerRepository.save(any()))
                .thenReturn(caretakerWithPhoto);

        List<PhotoLinkDTO> patchedPhotos =
                caretakerService.patchOfferPhotos(caretaker.getEmail(), Set.of(blob1), List.of(photo));

        // Then
        assertFalse(patchedPhotos.isEmpty());
        assertEquals(2, patchedPhotos.size());
        assertEquals(blob1, patchedPhotos.get(0).blob());
        assertEquals(newBlob, patchedPhotos.get(1).blob());
    }
}
