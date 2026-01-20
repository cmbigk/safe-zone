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
import org.springframework.web.multipart.MultipartFile;

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

    @BeforeEach
    void setUp() throws IOException {
        // Set upload directory for testing
        ReflectionTestUtils.setField(mediaService, "uploadDir", TEST_UPLOAD_DIR);

        // Create test upload directory
        Files.createDirectories(Paths.get(TEST_UPLOAD_DIR));

        testMedia = new Media();
        testMedia.setId("media123");
        testMedia.setFilename("test-image.jpg");
        testMedia.setOriginalFilename("original.jpg");
        testMedia.setContentType("image/jpeg");
        testMedia.setFileSize(1024L);
        testMedia.setFilePath(TEST_UPLOAD_DIR + "test-image.jpg");
        testMedia.setUploadedBy("user@example.com");
        testMedia.setProductId("product123");
    }

    @Test
    @DisplayName("Should successfully retrieve media by ID")
    void testGetMediaById_Success() {
        // Arrange
        when(mediaRepository.findById("media123")).thenReturn(Optional.of(testMedia));

        // Act
        MediaResponse response = mediaService.getMediaById("media123");

        // Assert
        assertNotNull(response);
        assertEquals("media123", response.getId());
        assertEquals("test-image.jpg", response.getFilename());
        assertEquals("original.jpg", response.getOriginalFilename());
        assertEquals("image/jpeg", response.getContentType());
        
        verify(mediaRepository).findById("media123");
    }

    @Test
    @DisplayName("Should throw exception when media not found by ID")
    void testGetMediaById_NotFound() {
        // Arrange
        when(mediaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.getMediaById("nonexistent");
        });

        verify(mediaRepository).findById("nonexistent");
    }

    @Test
    @DisplayName("Should retrieve all media for a product")
    void testGetMediaByProductId_Success() {
        // Arrange
        Media media2 = new Media();
        media2.setId("media456");
        media2.setFilename("product-image-2.jpg");
        media2.setProductId("product123");

        when(mediaRepository.findByProductId("product123"))
                .thenReturn(Arrays.asList(testMedia, media2));

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByProductId("product123");

        // Assert
        assertNotNull(mediaList);
        assertEquals(2, mediaList.size());
        assertTrue(mediaList.stream().allMatch(m -> m.getProductId().equals("product123")));
        
        verify(mediaRepository).findByProductId("product123");
    }

    @Test
    @DisplayName("Should retrieve all media uploaded by a user")
    void testGetMediaByUser_Success() {
        // Arrange
        Media media2 = new Media();
        media2.setId("media456");
        media2.setFilename("user-upload-2.jpg");
        media2.setUploadedBy("user@example.com");

        when(mediaRepository.findByUploadedBy("user@example.com"))
                .thenReturn(Arrays.asList(testMedia, media2));

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByUser("user@example.com");

        // Assert
        assertNotNull(mediaList);
        assertEquals(2, mediaList.size());
        assertTrue(mediaList.stream().allMatch(m -> m.getUploadedBy().equals("user@example.com")));
        
        verify(mediaRepository).findByUploadedBy("user@example.com");
    }

    @Test
    @DisplayName("Should reject empty file upload")
    void testUploadMedia_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            mediaService.uploadMedia(emptyFile, "user@example.com", "product123");
        });

        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void testUploadMedia_FileTooLarge() {
        // Arrange
        byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB (exceeds 2MB limit)
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mediaService.uploadMedia(largeFile, "user@example.com", "product123");
        });

        assertTrue(exception.getMessage().contains("File size exceeds 2MB limit"));
        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    @DisplayName("Should delete media and its file")
    void testDeleteMedia_Success() throws IOException {
        // Arrange
        // Create a temporary file for testing deletion
        Path testFile = Paths.get(TEST_UPLOAD_DIR, "test-image.jpg");
        Files.write(testFile, "test content".getBytes());
        
        when(mediaRepository.findById("media123")).thenReturn(Optional.of(testMedia));
        doNothing().when(mediaRepository).deleteById("media123");

        // Act
        mediaService.deleteMedia("media123");

        // Assert
        verify(mediaRepository).findById("media123");
        verify(mediaRepository).deleteById("media123");
        assertFalse(Files.exists(testFile), "File should be deleted");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent media")
    void testDeleteMedia_NotFound() {
        // Arrange
        when(mediaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.deleteMedia("nonexistent");
        });

        verify(mediaRepository).findById("nonexistent");
        verify(mediaRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should return empty list when product has no media")
    void testGetMediaByProductId_EmptyList() {
        // Arrange
        when(mediaRepository.findByProductId("product999")).thenReturn(Arrays.asList());

        // Act
        List<MediaResponse> mediaList = mediaService.getMediaByProductId("product999");

        // Assert
        assertNotNull(mediaList);
        assertTrue(mediaList.isEmpty());
        
        verify(mediaRepository).findByProductId("product999");
    }
}
