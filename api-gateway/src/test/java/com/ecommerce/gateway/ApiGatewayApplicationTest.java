package com.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }

    @Test
    void mainMethodRuns() {
        // Test that main method can be invoked without errors
        String[] args = {};
        // Just verify the class and main method exist
        assertDoesNotThrow(() -> ApiGatewayApplication.main(args));
    }
}