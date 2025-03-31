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

/**
 * Since v1.17.0, Testcontainers has shifted to SLF4J as the default logging facade.
 * All Testcontainers internal logs (like startup, container creation, etc.) are logged via SLF4J.
 * If your project has an SLF4J binding (like Logback), logs will automatically appear.
 * 
 * That's why, logs from org.testcontainers.*, tc.postgres:15, tc.testcontainers/ryuk:0.5.1 would 
 * be appearing even without withLogConsumer().
 * 
 * You are seeing two types of loggers in your output:
 * 
 * 1. org.testcontainers.*
 * These are Testcontainers' own internal framework logs.They originate from the Java classes inside Testcontainers library itself.
 * These logs will only appear if you have this in logback.xml:
 * <logger name="org.testcontainers" level="INFO"/>
 * They help you understand what Testcontainers framework is doing internally (image pulling, Docker client strategy, etc.).
 * 
 * tc.postgres:15
 * This is not a Java package logger. It is the logger name assigned to the actual running Docker container.
 * Format: tc.<image-name>:<tag>
 * This logger is created by Testcontainers' Slf4jLogConsumer internally if SLF4J is present, even if you don't manually attach a withLogConsumer().
 * It is used to log the container's lifecycle events — like when the container starts, stops, and its health status.
 */
@Testcontainers
public class LogbackIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(LogbackIntegrationTest.class);
    
    /*
     * withLogConsumer() is still required if you want to:
     * Capture and redirect the container stdout/stderr logs (application logs inside the container) to the application logger.
     */
    
    @Container
    private static final PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
        //.withLogConsumer(new Slf4jLogConsumer(logger)); // Attach logs to SLF4J

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
    }
}