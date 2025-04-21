package com.example.waitstrategies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresWaitStrategy {

	private static final Logger logger = LoggerFactory.getLogger(PostgresWaitStrategy.class);





	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
    .withDatabaseName("testdb")
    .withUsername("user")
    .withPassword("password")
    .withUrlParam("sslmode", "disable")
    // Liveness check (port)
    .waitingFor(Wait.forListeningPort()
        .withStartupTimeout(Duration.ofSeconds(60)))
    // Readiness check (logs)
    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1)
        .withStartupTimeout(Duration.ofSeconds(60)));

	
	//@Disabled
	@Test
	void testConnection() throws SQLException {
		logger.info("Running testWithWaitingStrategy...");
		try (Connection conn = DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(),
				postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword())) {
			conn.createStatement().execute("SELECT 1");
			logger.info("Database connected successfully.");
		}
	}
	
	@Test
    void testPostgresConnection() throws SQLException {
        logger.info("JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
        
        // Retry logic for connection
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection conn = DriverManager.getConnection(
            		postgreSQLContainer.getJdbcUrl(),
            		postgreSQLContainer.getUsername(),
            		postgreSQLContainer.getPassword())) {
                
                conn.createStatement().execute("SELECT 1");
                logger.info("✅ Database connection successful (attempt {}/{})", attempt, maxAttempts);
                return;
            } catch (SQLException e) {
                logger.warn("⚠️ Connection failed (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
                if (attempt == maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }
}
