package com.ecommerce.userservice.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private static final int STATUS = 404;
    private static final String MESSAGE = "Resource not found";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        ErrorResponse response = new ErrorResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        ErrorResponse response = new ErrorResponse(STATUS, MESSAGE, TIMESTAMP);
        
        assertEquals(STATUS, response.getStatus());
        assertEquals(MESSAGE, response.getMessage());
        assertEquals(TIMESTAMP, response.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        ErrorResponse response = new ErrorResponse();
        
        response.setStatus(STATUS);
        response.setMessage(MESSAGE);
        response.setTimestamp(TIMESTAMP);
        
        assertEquals(STATUS, response.getStatus());
        assertEquals(MESSAGE, response.getMessage());
        assertEquals(TIMESTAMP, response.getTimestamp());
    }
}
