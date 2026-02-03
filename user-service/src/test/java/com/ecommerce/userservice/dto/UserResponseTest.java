package com.ecommerce.userservice.dto;

import com.ecommerce.userservice.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    private static final String ID = "user-1";
    private static final String EMAIL = "user@example.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "1234567890";
    private static final UserRole ROLE = UserRole.CLIENT;
    private static final String AVATAR_URL = "avatar.jpg";
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime UPDATED_AT = LocalDateTime.now();

    @Test
    void testNoArgsConstructor() {
        UserResponse response = new UserResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        UserResponse response = new UserResponse(
                ID, EMAIL, FIRST_NAME, LAST_NAME, PHONE, ROLE, AVATAR_URL,
                CREATED_AT, UPDATED_AT
        );
        
        assertEquals(ID, response.getId());
        assertEquals(EMAIL, response.getEmail());
        assertEquals(FIRST_NAME, response.getFirstName());
        assertEquals(LAST_NAME, response.getLastName());
        assertEquals(PHONE, response.getPhone());
        assertEquals(ROLE, response.getRole());
        assertEquals(AVATAR_URL, response.getAvatarUrl());
        assertEquals(CREATED_AT, response.getCreatedAt());
        assertEquals(UPDATED_AT, response.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        UserResponse response = new UserResponse();
        
        response.setId(ID);
        response.setEmail(EMAIL);
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setPhone(PHONE);
        response.setRole(ROLE);
        response.setAvatarUrl(AVATAR_URL);
        response.setCreatedAt(CREATED_AT);
        response.setUpdatedAt(UPDATED_AT);
        
        assertEquals(ID, response.getId());
        assertEquals(EMAIL, response.getEmail());
        assertEquals(FIRST_NAME, response.getFirstName());
        assertEquals(LAST_NAME, response.getLastName());
        assertEquals(PHONE, response.getPhone());
        assertEquals(ROLE, response.getRole());
        assertEquals(AVATAR_URL, response.getAvatarUrl());
        assertEquals(CREATED_AT, response.getCreatedAt());
        assertEquals(UPDATED_AT, response.getUpdatedAt());
    }
}
