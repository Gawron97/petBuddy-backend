package com.example.petbuddybackend.service.photo;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.repository.photo.PhotoLinkRepository;
import com.example.petbuddybackend.utils.exception.throweable.photo.InvalidPhotoException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.apache.tika.Tika;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PhotoServiceTest {

    private static final String FILE_NAME = "test.jpg";
    private static final String BLOB_PATH = "valid/blob/path";
    private static final String PHOTO_URL = "http://signedurl.com";

    @Value("${firebase.photo.directory}")
    private String PHOTO_DIRECTORY;

    @Autowired
    private FirebasePhotoService firebasePhotoService;

    @MockBean
    private FirebaseApp firebaseApp;

    @MockBean
    private PhotoLinkRepository photoRepository;

    @MockBean
    private Tika tika;

    @Mock
    private Blob mockBlob;

    @Mock
    private StorageClient mockStorageClient;

    @Mock
    private Bucket mockBucket;

    private MockedStatic<StorageClient> storageClientMock;
    private MockMultipartFile validPhoto;

    @BeforeEach
    public void setUp() {
        validPhoto = new MockMultipartFile(
                "file",
                FILE_NAME,
                "image/jpeg",
                new byte[]{0, 1, 2}
        );

        storageClientMock = mockStatic(StorageClient.class);

        when(StorageClient.getInstance(firebaseApp)).thenReturn(mockStorageClient);
        when(mockStorageClient.bucket()).thenReturn(mockBucket);
    }

    @AfterEach
    public void tearDown() {
        storageClientMock.close();
    }


    @Test
    public void testUploadPhoto_validFile_shouldUploadSuccessfully() throws IOException {
        when(mockBucket.create(any(String.class), any(ByteArrayInputStream.class), any(String.class))).thenReturn(mockBlob);
        when(mockBlob.signUrl(anyLong(), any())).thenReturn(new URL("http://signedurl.com"));
        when(tika.detect(any(InputStream.class))).thenReturn("image/jpeg");
        when(photoRepository.save(any(PhotoLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PhotoLink result = firebasePhotoService.uploadPhoto(validPhoto);

        assertNotNull(result);
        assertNotEquals(PHOTO_DIRECTORY + "/" + FILE_NAME, result.getBlob());
        assertTrue(result.getBlob().startsWith(PHOTO_DIRECTORY));
        verify(photoRepository, times(1)).save(result);
    }

    @Test
    public void testUploadPhoto_invalidFile_shouldThrowInvalidPhotoException() throws IOException {
        MockMultipartFile invalidPhoto = new MockMultipartFile("file", "", "text/plain", new byte[]{0});
        when(tika.detect(any(InputStream.class))).thenReturn("text/plain");

        assertThrows(InvalidPhotoException.class,
                () -> firebasePhotoService.uploadPhoto(invalidPhoto)
        );
    }

    @Test
    public void testDeletePhoto_validBlob_shouldDeleteSuccessfully() {
        PhotoLink photo = new PhotoLink(BLOB_PATH, PHOTO_URL, LocalDateTime.now().plusDays(10));
        when(photoRepository.findById(BLOB_PATH)).thenReturn(Optional.of(photo));
        when(mockBucket.get(BLOB_PATH)).thenReturn(mockBlob);

        firebasePhotoService.deletePhoto(BLOB_PATH);

        verify(mockBlob, times(1)).delete();
        verify(photoRepository, times(1)).delete(photo);
    }

    @Test
    public void testDeletePhoto_nonExistentBlob_shouldNotThrowException() {
        String nonExistentBlob = "non/existent/blob";

        when(photoRepository.findById(nonExistentBlob)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> firebasePhotoService.deletePhoto(nonExistentBlob));
    }

    @Test
    public void testUpdatePhotoExpiration_withinThreshold_shouldNotRenew() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(20);
        PhotoLink photo = new PhotoLink("valid/blob/path", "http://signedurl.com", expiresAt);

        firebasePhotoService.updatePhotoExpiration(photo);
        verify(photoRepository).save(any());
    }

    @Test
    public void testUpdatePhotoExpiration_expired_shouldRenew() throws MalformedURLException {
        LocalDateTime expiresAt = LocalDateTime.now().minusSeconds(1);
        PhotoLink photo = new PhotoLink("valid/blob/path", "http://signedurl.com", expiresAt);
        when(mockBucket.get(photo.getBlob())).thenReturn(mockBlob);
        when(mockBlob.signUrl(anyLong(), any())).thenReturn(new URL("http://new-signedurl.com"));

        firebasePhotoService.updatePhotoExpiration(photo);

        verify(photoRepository, times(1)).save(photo);
    }
}
