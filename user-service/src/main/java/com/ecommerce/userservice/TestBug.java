package com.ecommerce.userservice;
import java.sql.*;

public class TestBug {
    public void sqlInjection(String input) throws SQLException {
        Statement stmt = DriverManager.getConnection("jdbc:mysql://localhost/db").createStatement();
        stmt.executeQuery("SELECT * FROM users WHERE id = " + input); // SQL injection!
    }
}
