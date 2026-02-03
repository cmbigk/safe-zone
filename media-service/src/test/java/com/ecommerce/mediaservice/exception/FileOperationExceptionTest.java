package com.ecommerce.mediaservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "File operation failed";
        FileOperationException exception = new FileOperationException(message);
        
        assertEquals(message, exception.getMessage());
    }
}
