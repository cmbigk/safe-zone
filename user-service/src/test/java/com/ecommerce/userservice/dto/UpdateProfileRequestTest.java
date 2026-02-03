package com.ecommerce.userservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateProfileRequestTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "1234567890";
    private static final String AVATAR_URL = "avatar.jpg";

    @Test
    void testNoArgsConstructor() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgsConstructor() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                FIRST_NAME, LAST_NAME, PHONE, AVATAR_URL
        );
        
        assertEquals(FIRST_NAME, request.getFirstName());
        assertEquals(LAST_NAME, request.getLastName());
        assertEquals(PHONE, request.getPhone());
        assertEquals(AVATAR_URL, request.getAvatarUrl());
    }

    @Test
    void testSettersAndGetters() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        request.setPhone(PHONE);
        request.setAvatarUrl(AVATAR_URL);
        
        assertEquals(FIRST_NAME, request.getFirstName());
        assertEquals(LAST_NAME, request.getLastName());
        assertEquals(PHONE, request.getPhone());
        assertEquals(AVATAR_URL, request.getAvatarUrl());
    }
}
