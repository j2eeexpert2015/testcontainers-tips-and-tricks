package com.example.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class NoLoggingDemoTest {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
        //.withLogConsumer(new Slf4jLogConsumer(logger)); // Attach logs to SLF4J

    @Test
    void test() {
        System.out.println("Test running - container logs appear automatically!");
    }
    
    @Test
    void testDatabaseConnection() throws SQLException {
    	System.out.println("JDBC URL: {}"+ postgres.getJdbcUrl());
        
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {
            
            conn.createStatement().execute("SELECT 1");
            System.out.println("Query succeeded! ");
        }
    }
}