package com.ecommerce.mediaservice.service;

import com.ecommerce.mediaservice.dto.MediaResponse;
import com.ecommerce.mediaservice.exception.ResourceNotFoundException;
import com.ecommerce.mediaservice.model.Media;
import com.ecommerce.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaService
 * Tests the core business logic of media file management including upload validation, retrieval, and deletion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MediaService Tests")
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private MediaService mediaService;

    private Media testMedia;
    private static final String TEST_UPLOAD_DIR = "test-uploads/";
    private static final String TEST_MEDIA_ID = "media123";
    private static final String TEST_FILENAME = "test-image.jpg";
    private static final String TEST_ORIGINAL_FILENAME = "original.jpg";
    private static final String TEST_CONTENT_TYPE = "image/jpeg";
    private static final String TEST_UPLOADED_BY = "user@example.com";
    private static final String TEST_PRODUCT_ID = "product123";
    private static final String NONEXISTENT_ID = "nonexistent";
    private static final String PRODUCT_WITHOUT_MEDIA = "product999";

    @BeforeEach
    void setUp() throws IOException {
        // Set upload directory for testing
        ReflectionTestUtils.setField(mediaService, "uploadDir", TEST_UPLOAD_DIR);

        // Create test upload directory
        Files.createDirectories(Paths.get(TEST_UPLOAD_DIR));

        // Initialize test media object
        testMedia = new Media();
        testMedia.setId(TEST_MEDIA_ID);
        testMedia.setFilename(TEST_FILENAME);
        testMedia.setOriginalFilename(TEST_ORIGINAL_FILENAME);
        testMedia.setContentType(TEST_CONTENT_TYPE);
        testMedia.setFileSize(1024L);
        testMedia.setFilePath(TEST_UPLOAD_DIR + TEST_FILENAME);
        testMedia.setUploadedBy(TEST_UPLOADED_BY);
        testMedia.setProductId(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("Should successfully retrieve media by ID")
    void testGetMediaByIdSuccess() {
        // Arrange
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));

        // Act
        MediaResponse response = mediaService.getMediaById(TEST_MEDIA_ID);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_MEDIA_ID, response.getId());
        assertEquals(TEST_FILENAME, response.getFilename());
        assertEquals(TEST_ORIGINAL_FILENAME, response.getOriginalFilename());
        assertEquals(TEST_CONTENT_TYPE, response.getContentType());
        
        verify(mediaRepository).findById(TEST_MEDIA_ID);
    }

    @Test
    @DisplayName("Should throw exception when media not found by ID")
    void testGetMediaByIdNotFound() {
        // Arrange
        when(mediaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            mediaService.getMediaById(NONEXISTENT_ID)
        );

        verify(mediaRepository).findById(NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Should retrieve all media for a product")
    void testGetMediaByProductIdSuccess() {
        // Arrange
        Media media2 = new Media();
        media2.setId("media456");
        media2.setFilename("product-image-2.jpg");
        media2.setProductId(TEST_PRODUCT_ID);

        when(mediaRepository.findByProductId(TEST_PRODUCT_ID))
                .thenReturn(Arrays.asList(testMedia, media2));

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByProductId(TEST_PRODUCT_ID);

        // Assert
        assertNotNull(mediaList);
        assertEquals(2, mediaList.size());
        assertTrue(mediaList.stream().allMatch(m -> m.getProductId().equals(TEST_PRODUCT_ID)));
        
        verify(mediaRepository).findByProductId(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("Should retrieve all media uploaded by a user")
    void testGetMediaByUserSuccess() {
        // Arrange
        Media media2 = new Media();
        media2.setId("media456");
        media2.setFilename("user-upload-2.jpg");
        media2.setUploadedBy(TEST_UPLOADED_BY);

        when(mediaRepository.findByUploadedBy(TEST_UPLOADED_BY))
                .thenReturn(Arrays.asList(testMedia, media2));

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByUser(TEST_UPLOADED_BY);

        // Assert
        assertNotNull(mediaList);
        assertEquals(2, mediaList.size());
        assertTrue(mediaList.stream().allMatch(m -> m.getUploadedBy().equals(TEST_UPLOADED_BY)));
        
        verify(mediaRepository).findByUploadedBy(TEST_UPLOADED_BY);
    }

    @Test
    @DisplayName("Should reject empty file upload")
    void testUploadMediaEmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                TEST_CONTENT_TYPE,
                new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            mediaService.uploadMedia(emptyFile, TEST_UPLOADED_BY, TEST_PRODUCT_ID)
        );

        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void testUploadMediaFileTooLarge() {
        // Arrange
        byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB (exceeds 2MB limit)
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                TEST_CONTENT_TYPE,
                largeContent
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            mediaService.uploadMedia(largeFile, TEST_UPLOADED_BY, TEST_PRODUCT_ID)
        );

        assertTrue(exception.getMessage().contains("File size exceeds 2MB limit"));
        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    @DisplayName("Should delete media and its file")
    void testDeleteMediaSuccess() throws IOException {
        // Arrange
        // Create a temporary file for testing deletion
        Path testFile = Paths.get(TEST_UPLOAD_DIR, TEST_FILENAME);
        Files.write(testFile, "test content".getBytes());
        
        when(mediaRepository.findById(TEST_MEDIA_ID)).thenReturn(Optional.of(testMedia));
        doNothing().when(mediaRepository).deleteById(TEST_MEDIA_ID);

        // Act
        mediaService.deleteMedia(TEST_MEDIA_ID);

        // Assert
        verify(mediaRepository).findById(TEST_MEDIA_ID);
        verify(mediaRepository).deleteById(TEST_MEDIA_ID);
        assertFalse(Files.exists(testFile), "File should be deleted");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent media")
    void testDeleteMediaNotFound() {
        // Arrange
        when(mediaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            mediaService.deleteMedia(NONEXISTENT_ID)
        );

        verify(mediaRepository).findById(NONEXISTENT_ID);
        verify(mediaRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should return empty list when product has no media")
    void testGetMediaByProductIdEmptyList() {
        // Arrange
        when(mediaRepository.findByProductId(PRODUCT_WITHOUT_MEDIA)).thenReturn(Arrays.asList());

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByProductId(PRODUCT_WITHOUT_MEDIA);

        // Assert
        assertNotNull(mediaList);
        assertTrue(mediaList.isEmpty());
        
        verify(mediaRepository).findByProductId(PRODUCT_WITHOUT_MEDIA);
    }
}
