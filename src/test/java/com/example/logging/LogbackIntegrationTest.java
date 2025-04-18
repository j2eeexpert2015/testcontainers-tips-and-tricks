package com.example.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class LogbackIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(LogbackIntegrationTest.class);
    
    /*
     * withLogConsumer() is still required if you want to:
     * Capture and redirect the container stdout/stderr logs (application logs inside the container) to the application logger.
     */
    
    @Container
    private static final PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15")
        .withLogConsumer(new Slf4jLogConsumer(logger));

    @Test
    void test() {
        logger.info("Test running - container logs appear automatically!");
    }
    
    @Test
    void testDatabaseConnection() throws SQLException {
        logger.info("JDBC URL: {}", postgres.getJdbcUrl());
        
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {
            
            conn.createStatement().execute("SELECT 1");
            logger.info("Query succeeded!");
        }

        // Use getLogs() to inspect output after container has started
        String combinedLogs = postgres.getLogs();
        logger.info("Captured logs using getLogs():\n{}", combinedLogs);

        // Optional: assertion on log content
        assertTrue(combinedLogs.contains("database system is ready to accept connections"),
                "PostgreSQL startup log not found in container logs");
    }
}