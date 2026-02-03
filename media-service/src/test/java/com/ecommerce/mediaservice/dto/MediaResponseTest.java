package com.ecommerce.mediaservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MediaResponseTest {

    private static final String ID = "media-1";
    private static final String FILENAME = "test-file.jpg";
    private static final String ORIGINAL_FILENAME = "original.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final Long FILE_SIZE = 1024L;
    private static final String URL = "http://localhost/files/test-file.jpg";
    private static final String UPLOADED_BY = "user@example.com";
    private static final String PRODUCT_ID = "product-1";
    private static final LocalDateTime UPLOADED_AT = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        MediaResponse response = new MediaResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        MediaResponse response = new MediaResponse(
                ID, FILENAME, ORIGINAL_FILENAME, CONTENT_TYPE, FILE_SIZE,
                URL, UPLOADED_BY, PRODUCT_ID, UPLOADED_AT
        );
        
        assertEquals(ID, response.getId());
        assertEquals(FILENAME, response.getFilename());
        assertEquals(ORIGINAL_FILENAME, response.getOriginalFilename());
        assertEquals(CONTENT_TYPE, response.getContentType());
        assertEquals(FILE_SIZE, response.getFileSize());
        assertEquals(URL, response.getUrl());
        assertEquals(UPLOADED_BY, response.getUploadedBy());
        assertEquals(PRODUCT_ID, response.getProductId());
        assertEquals(UPLOADED_AT, response.getUploadedAt());
    }

    @Test
    void testSettersAndGetters() {
        MediaResponse response = new MediaResponse();
        
        response.setId(ID);
        response.setFilename(FILENAME);
        response.setOriginalFilename(ORIGINAL_FILENAME);
        response.setContentType(CONTENT_TYPE);
        response.setFileSize(FILE_SIZE);
        response.setUrl(URL);
        response.setUploadedBy(UPLOADED_BY);
        response.setProductId(PRODUCT_ID);
        response.setUploadedAt(UPLOADED_AT);
        
        assertEquals(ID, response.getId());
        assertEquals(FILENAME, response.getFilename());
        assertEquals(ORIGINAL_FILENAME, response.getOriginalFilename());
        assertEquals(CONTENT_TYPE, response.getContentType());
        assertEquals(FILE_SIZE, response.getFileSize());
        assertEquals(URL, response.getUrl());
        assertEquals(UPLOADED_BY, response.getUploadedBy());
        assertEquals(PRODUCT_ID, response.getProductId());
        assertEquals(UPLOADED_AT, response.getUploadedAt());
    }
}
