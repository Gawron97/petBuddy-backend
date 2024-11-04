package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.repository.photo.PhotoLinkRepository;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PhotoDeletionScheduledIntegrationTest {

    @Autowired
    private PhotoDeletionScheduled photoDeletionScheduled;

    @Autowired
    private PhotoLinkRepository photoRepository;

    @MockBean
    private FirebaseApp firebaseApp;

    @Mock
    private StorageClient mockStorageClient;

    @Mock
    private Bucket mockBucket;

    private MockedStatic<StorageClient> storageClientMock;

    @BeforeEach
    void setUp() {
        storageClientMock = mockStatic(StorageClient.class);
        when(StorageClient.getInstance(firebaseApp)).thenReturn(mockStorageClient);
        when(mockStorageClient.bucket()).thenReturn(mockBucket);
    }

    @AfterEach
    void tearDown() {
        storageClientMock.close();
    }

    @Test
    void testRemovePhotosMarkedForDeletion_shouldSucceed() {
        List<PhotoLink> photosToDelete = List.of(
                new PhotoLink("1", "photo1", LocalDateTime.now(), null),
                new PhotoLink("2", "photo2", LocalDateTime.now(), LocalDateTime.now().minusDays(10)),
                new PhotoLink("1", "photo1", LocalDateTime.now(), LocalDateTime.now().plusDays(10)),
                new PhotoLink("2", "photo2", LocalDateTime.now(), LocalDateTime.now())
        );
        photoRepository.saveAll(photosToDelete);
        photoDeletionScheduled.terminatePhotos();
    }
}
