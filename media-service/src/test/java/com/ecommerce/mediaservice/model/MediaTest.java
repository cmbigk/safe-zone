package com.ecommerce.mediaservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    private static final String ID = "media-1";
    private static final String FILENAME = "stored-file.jpg";
    private static final String ORIGINAL_FILENAME = "original.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final Long FILE_SIZE = 1024L;
    private static final String FILE_PATH = "/uploads/stored-file.jpg";
    private static final String UPLOADED_BY = "user@example.com";
    private static final String PRODUCT_ID = "product-1";
    private static final LocalDateTime UPLOADED_AT = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        Media media = new Media();
        assertNotNull(media);
    }

    @Test
    void testAllArgsConstructor() {
        Media media = new Media(
                ID, FILENAME, ORIGINAL_FILENAME, CONTENT_TYPE, FILE_SIZE,
                FILE_PATH, UPLOADED_BY, PRODUCT_ID, UPLOADED_AT
        );

        assertEquals(ID, media.getId());
        assertEquals(FILENAME, media.getFilename());
        assertEquals(ORIGINAL_FILENAME, media.getOriginalFilename());
        assertEquals(CONTENT_TYPE, media.getContentType());
        assertEquals(FILE_SIZE, media.getFileSize());
        assertEquals(FILE_PATH, media.getFilePath());
        assertEquals(UPLOADED_BY, media.getUploadedBy());
        assertEquals(PRODUCT_ID, media.getProductId());
        assertEquals(UPLOADED_AT, media.getUploadedAt());
    }

    @Test
    void testSettersAndGetters() {
        Media media = new Media();

        media.setId(ID);
        media.setFilename(FILENAME);
        media.setOriginalFilename(ORIGINAL_FILENAME);
        media.setContentType(CONTENT_TYPE);
        media.setFileSize(FILE_SIZE);
        media.setFilePath(FILE_PATH);
        media.setUploadedBy(UPLOADED_BY);
        media.setProductId(PRODUCT_ID);
        media.setUploadedAt(UPLOADED_AT);

        assertEquals(ID, media.getId());
        assertEquals(FILENAME, media.getFilename());
        assertEquals(ORIGINAL_FILENAME, media.getOriginalFilename());
        assertEquals(CONTENT_TYPE, media.getContentType());
        assertEquals(FILE_SIZE, media.getFileSize());
        assertEquals(FILE_PATH, media.getFilePath());
        assertEquals(UPLOADED_BY, media.getUploadedBy());
        assertEquals(PRODUCT_ID, media.getProductId());
        assertEquals(UPLOADED_AT, media.getUploadedAt());
    }
}
