package com.ecommerce.mediaservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Media not found");
        
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Media not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleFileOperation() {
        FileOperationException ex = new FileOperationException("File upload failed");
        
        ResponseEntity<ErrorResponse> response = handler.handleFileOperation(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("File upload failed", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid file type");
        
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid file type", response.getBody().getMessage());
    }

    @Test
    void testHandleMaxSizeException() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(2097152);
        
        ResponseEntity<ErrorResponse> response = handler.handleMaxSizeException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("File size exceeds maximum limit"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
    }
}
