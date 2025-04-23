package com.example.singleton;

import com.example.singleton.AbstractContainerBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Inherits from the base class to use the shared container
class FirstServiceIntegrationTest extends AbstractContainerBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(FirstServiceIntegrationTest.class);

    @BeforeAll
    static void logContainerInfo() {
        // Log container details once before tests in this class run
        logger.info("FirstServiceIntegrationTest using container running at JDBC URL: {}", getJdbcUrl());
    }

    @Test
    void testConnectionToSharedDatabase() {
        logger.info(" Running test 'testConnectionToSharedDatabase' in FirstServiceIntegrationTest ");
        Assertions.assertTrue(MY_SQL_CONTAINER.isRunning(), "Container should be running");

        try (Connection connection = DriverManager.getConnection(
                getJdbcUrl(), // Use inherited method or direct access
                MY_SQL_CONTAINER.getUsername(),
                MY_SQL_CONTAINER.getPassword())) {

            Assertions.assertTrue(connection.isValid(1), "Connection should be valid");
            logger.info("   Successfully connected from FirstServiceIntegrationTest!");

        } catch (SQLException e) {
            logger.error("   Connection failed in FirstServiceIntegrationTest", e);
            Assertions.fail("Database connection failed", e);
        }
    }

    @Test
    void anotherTestInFirstService() {
        logger.info(" Running test 'anotherTestInFirstService' in FirstServiceIntegrationTest <<<");
        // This test also uses the SAME container instance
        Assertions.assertTrue(MY_SQL_CONTAINER.isRunning());
        logger.info("   Container confirmed running for second test in FirstServiceIntegrationTest.");
        // Add more assertions or logic as needed
    }
}