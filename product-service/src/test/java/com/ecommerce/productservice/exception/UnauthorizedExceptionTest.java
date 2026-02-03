package com.ecommerce.productservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Unauthorized access";
        UnauthorizedException exception = new UnauthorizedException(message);
        
        assertEquals(message, exception.getMessage());
    }
}
