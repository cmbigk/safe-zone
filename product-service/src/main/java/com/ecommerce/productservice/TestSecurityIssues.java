package com.ecommerce.productservice;

import java.sql.*;
import java.io.File;

/**
 * THIS FILE CONTAINS INTENTIONAL SECURITY VULNERABILITIES FOR TESTING
 * Purpose: Demonstrate that Quality Gate fails when code quality/security issues are detected
 * DO NOT USE IN PRODUCTION
 */
public class TestSecurityIssues {
    
    // CRITICAL: SQL Injection Vulnerability
    public ResultSet vulnerableQuery(String userId, String productId) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommerce");
        Statement stmt = conn.createStatement();
        
        // Security Hotspot: SQL injection through string concatenation
        String query = "SELECT * FROM products WHERE id = '" + productId + "' AND seller_id = '" + userId + "'";
        ResultSet rs = stmt.executeQuery(query);
        
        // Resources never closed - another issue
        return rs;
    }
    
    // CRITICAL: Hardcoded Credentials
    public void hardcodedSecrets() {
        String dbPassword = "admin123";
        String apiKey = "sk_live_51234567890abcdefghijk";
        String jwtSecret = "mySecretKey123!@#";
        
        // Using hardcoded credentials (security issue)
        System.out.println("Connecting with password: " + dbPassword);
    }
    
    // MAJOR: Unused variables (Code Smells)
    public void deadCode() {
        int unusedVariable1 = 10;
        String unusedVariable2 = "test";
        double unusedVariable3 = 3.14159;
        boolean unusedVariable4 = true;
        Object unusedVariable5 = new Object();
        
        // None of these variables are used
        System.out.println("Method completed");
    }
    
    // MAJOR: Empty catch block
    public void poorErrorHandling() {
        try {
            int result = 10 / 0; // Will throw ArithmeticException
            String data = null;
            data.length(); // Will throw NullPointerException
        } catch (Exception e) {
            // Empty catch block - swallows exceptions silently
        }
    }
    
    // CRITICAL: Path Traversal Vulnerability
    public File unsafeFileAccess(String filename) {
        // Security issue: user input directly used in file path
        File file = new File("/uploads/" + filename);
        return file;
    }
    
    // MAJOR: Possible NullPointerException
    public int nullPointerRisk(String input) {
        // No null check before using the parameter
        return input.length(); // Will crash if input is null
    }
    
    // MINOR: Duplicate code (similar to other methods)
    public void duplicatePattern1() {
        String value1 = "test";
        String value2 = "test";
        String value3 = "test";
        int count = 0;
        for (int i = 0; i < 10; i++) {
            count++;
        }
        System.out.println(count);
    }
    
    // MINOR: Duplicate code (similar to other methods)
    public void duplicatePattern2() {
        String value1 = "test";
        String value2 = "test";
        String value3 = "test";
        int count = 0;
        for (int i = 0; i < 10; i++) {
            count++;
        }
        System.out.println(count);
    }
    
    // MAJOR: Weak cryptography
    public String weakEncryption(String password) {
        // Using weak/outdated algorithm
        return password.hashCode() + "";
    }
}
