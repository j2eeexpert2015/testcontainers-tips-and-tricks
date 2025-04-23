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

// Also inherits from the base class
class SecondServiceIntegrationTest extends AbstractContainerBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SecondServiceIntegrationTest.class);

    @BeforeAll
    static void logContainerInfo() {
        // Log container details once before tests in this class run
        logger.info("SecondServiceIntegrationTest using container running at JDBC URL: {}", getJdbcUrl());
    }

    @Test
    void testAnotherServiceUsesSameDatabase() {
        logger.info(" Running test in SecondServiceIntegrationTest <<<");
        Assertions.assertTrue(MY_SQL_CONTAINER.isRunning(), "Container should still be running");
        logger.debug("   Attempting connection to: {}", getJdbcUrl()); // Example of DEBUG level

        // Verify connection
        try (Connection connection = DriverManager.getConnection(
                getJdbcUrl(),
                MY_SQL_CONTAINER.getUsername(),
                MY_SQL_CONTAINER.getPassword())) {

            Assertions.assertTrue(connection.isValid(1), "Connection should be valid");
            logger.info("   Successfully connected from SecondServiceIntegrationTest!");

        } catch (SQLException e) {
            logger.error("   Connection failed in SecondServiceIntegrationTest", e);
            Assertions.fail("Database connection failed", e);
        }
    }
}