package com.ecommerce.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAlreadyExistsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "User already exists";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(message);
        
        assertEquals(message, exception.getMessage());
    }
}
