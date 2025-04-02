package com.example.waitstrategies;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Demonstrates the usage of Wait.forHealthcheck() strategy with MySQL official image.
 * 
 * MySQL Docker image defines a HEALTHCHECK instruction by default.
 * This strategy will wait until the container health status is "healthy".
 */
@Testcontainers
public class MySQLHealthCheckWaitStrategyTest {

    private static final Logger logger = LoggerFactory.getLogger(MySQLHealthCheckWaitStrategyTest.class);

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password")
            .withExposedPorts(3306)
            .waitingFor(Wait.forHealthcheck()); // Healthy check
            

    /**
     * Test verifies that MySQL container is healthy and ready before connecting.
     */
    @Test
    void testMySQLHealthCheckStrategy() throws SQLException {
        String jdbcUrl = mysql.getJdbcUrl();
        logger.info("✅ MySQL is running at {}", jdbcUrl);

        try (Connection conn = DriverManager.getConnection(
                jdbcUrl,
                mysql.getUsername(),
                mysql.getPassword())) {

            assertTrue(conn.isValid(2), "MySQL connection should be valid");
            logger.info("✅ Successfully connected to MySQL after health check readiness.");
        }
    }
}
