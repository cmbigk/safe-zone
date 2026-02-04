package com.ecommerce.userservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    private static final String TOKEN = "test-token-123";
    private static final String TYPE = "Bearer";
    private static final UserResponse USER = new UserResponse();

    @Test
    void testNoArgsConstructor() {
        AuthResponse response = new AuthResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        AuthResponse response = new AuthResponse(TOKEN, TYPE, USER);
        
        assertEquals(TOKEN, response.getToken());
        assertEquals(TYPE, response.getType());
        assertEquals(USER, response.getUser());
    }

    @Test
    void testTwoArgsConstructor() {
        AuthResponse response = new AuthResponse(TOKEN, USER);
        
        assertEquals(TOKEN, response.getToken());
        assertEquals(TYPE, response.getType());
        assertEquals(USER, response.getUser());
    }

    @Test
    void testSettersAndGetters() {
        AuthResponse response = new AuthResponse();
        
        response.setToken(TOKEN);
        response.setType(TYPE);
        response.setUser(USER);
        
        assertEquals(TOKEN, response.getToken());
        assertEquals(TYPE, response.getType());
        assertEquals(USER, response.getUser());
    }
}
