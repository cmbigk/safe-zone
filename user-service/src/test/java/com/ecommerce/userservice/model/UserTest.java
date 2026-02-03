package com.ecommerce.userservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final String ID = "user-1";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "hashedPassword123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "1234567890";
    private static final UserRole ROLE = UserRole.CLIENT;
    private static final String AVATAR_URL = "avatar.jpg";
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime UPDATED_AT = LocalDateTime.now();
    private static final boolean ENABLED = true;

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertTrue(user.isEnabled()); // Default value
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(
                ID, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE,
                ROLE, AVATAR_URL, CREATED_AT, UPDATED_AT, ENABLED
        );

        assertEquals(ID, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(PHONE, user.getPhone());
        assertEquals(ROLE, user.getRole());
        assertEquals(AVATAR_URL, user.getAvatarUrl());
        assertEquals(CREATED_AT, user.getCreatedAt());
        assertEquals(UPDATED_AT, user.getUpdatedAt());
        assertEquals(ENABLED, user.isEnabled());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();

        user.setId(ID);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPhone(PHONE);
        user.setRole(ROLE);
        user.setAvatarUrl(AVATAR_URL);
        user.setCreatedAt(CREATED_AT);
        user.setUpdatedAt(UPDATED_AT);
        user.setEnabled(ENABLED);

        assertEquals(ID, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(PHONE, user.getPhone());
        assertEquals(ROLE, user.getRole());
        assertEquals(AVATAR_URL, user.getAvatarUrl());
        assertEquals(CREATED_AT, user.getCreatedAt());
        assertEquals(UPDATED_AT, user.getUpdatedAt());
        assertEquals(ENABLED, user.isEnabled());
    }

    @Test
    void testEnabledFlag() {
        User user = new User();
        assertTrue(user.isEnabled());

        user.setEnabled(false);
        assertFalse(user.isEnabled());

        user.setEnabled(true);
        assertTrue(user.isEnabled());
    }
}
