package com.ecommerce.userservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";

    @Test
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgsConstructor() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        LoginRequest request = new LoginRequest();
        
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }
}
