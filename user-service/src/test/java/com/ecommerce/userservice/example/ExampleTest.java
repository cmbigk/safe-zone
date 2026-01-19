package com.ecommerce.userservice.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Example JUnit 5 Test Class
 * 
 * This demonstrates best practices for backend testing in the pipeline.
 * Tests run during 'mvn test' phase and generate XML reports in target/surefire-reports/
 * 
 * Key Testing Patterns:
 * - Use descriptive test names with @DisplayName
 * - Follow AAA pattern: Arrange, Act, Assert
 * - Test edge cases and error conditions
 * - Use @BeforeEach for test setup
 * - Use @AfterEach for cleanup
 */
@DisplayName("Example Service Tests")
public class ExampleTest {
    
    private String testData;
    
    @BeforeEach
    public void setUp() {
        // Arrange: Initialize test data before each test
        testData = "Hello, World!";
    }
    
    @AfterEach
    public void tearDown() {
        // Cleanup after each test
        testData = null;
    }
    
    @Test
    @DisplayName("Should pass basic assertion test")
    public void testBasicAssertion() {
        // Arrange
        String expected = "Hello, World!";
        
        // Act
        String actual = testData;
        
        // Assert
        assertEquals(expected, actual, "Test data should match expected value");
    }
    
    @Test
    @DisplayName("Should validate string operations")
    public void testStringOperations() {
        // Act
        String uppercase = testData.toUpperCase();
        
        // Assert
        assertEquals("HELLO, WORLD!", uppercase);
        assertTrue(testData.contains("Hello"));
        assertFalse(testData.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle null values correctly")
    public void testNullHandling() {
        // Arrange
        String nullString = null;
        
        // Assert
        assertNull(nullString, "Null string should be null");
        assertNotNull(testData, "Test data should not be null");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid input")
    public void testExceptionHandling() {
        // Assert that exception is thrown
        assertThrows(NullPointerException.class, () -> {
            String nullString = null;
            nullString.length(); // This will throw NullPointerException
        }, "Should throw NullPointerException for null string");
    }
    
    @Test
    @DisplayName("Should validate numeric calculations")
    public void testNumericOperations() {
        // Arrange
        int a = 5;
        int b = 10;
        
        // Act
        int sum = a + b;
        int product = a * b;
        
        // Assert
        assertEquals(15, sum, "Sum should be 15");
        assertEquals(50, product, "Product should be 50");
        assertTrue(sum < product, "Sum should be less than product");
    }
    
    @Test
    @DisplayName("Should validate array operations")
    public void testArrayOperations() {
        // Arrange
        int[] numbers = {1, 2, 3, 4, 5};
        
        // Assert
        assertEquals(5, numbers.length, "Array should have 5 elements");
        assertEquals(1, numbers[0], "First element should be 1");
        assertEquals(5, numbers[4], "Last element should be 5");
    }
    
    @Test
    @DisplayName("Should validate boolean logic")
    public void testBooleanLogic() {
        // Arrange
        boolean isValid = true;
        boolean isInvalid = false;
        
        // Assert
        assertTrue(isValid, "isValid should be true");
        assertFalse(isInvalid, "isInvalid should be false");
        assertTrue(isValid && !isInvalid, "Logical AND should work correctly");
    }
}
