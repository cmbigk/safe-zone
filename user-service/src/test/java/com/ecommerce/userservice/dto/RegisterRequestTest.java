package com.ecommerce.userservice.dto;

import com.ecommerce.userservice.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "1234567890";
    private static final String AVATAR_URL = "avatar.jpg";
    private static final UserRole ROLE = UserRole.CLIENT;

    @Test
    void testNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgsConstructor() {
        RegisterRequest request = new RegisterRequest(
                EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE, AVATAR_URL, ROLE
        );
        
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
        assertEquals(FIRST_NAME, request.getFirstName());
        assertEquals(LAST_NAME, request.getLastName());
        assertEquals(PHONE, request.getPhone());
        assertEquals(AVATAR_URL, request.getAvatarUrl());
        assertEquals(ROLE, request.getRole());
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest request = new RegisterRequest();
        
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        request.setPhone(PHONE);
        request.setAvatarUrl(AVATAR_URL);
        request.setRole(ROLE);
        
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
        assertEquals(FIRST_NAME, request.getFirstName());
        assertEquals(LAST_NAME, request.getLastName());
        assertEquals(PHONE, request.getPhone());
        assertEquals(AVATAR_URL, request.getAvatarUrl());
        assertEquals(ROLE, request.getRole());
    }
}
