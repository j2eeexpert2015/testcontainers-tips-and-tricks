package com.example.imagesubstitution;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresAutomaticImageSubstitutionTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresAutomaticImageSubstitutionTest.class);
    private static final String EXPECTED_PREFIX = "ghcr.io/mrayandutta/postgres-demo/";
    private static final String EXPECTED_IMAGE = EXPECTED_PREFIX + "postgres:15";

    @Test
    void validateExplicitSubstitutionWithExcludes() {
        // Arrange
        DockerImageName imageName = DockerImageName.parse("postgres").withTag("15");
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(imageName)
                .withPassword("mysecretpassword")) {

            // Act
            postgres.start();
            String jdbcUrl = postgres.getJdbcUrl();
            String resolvedImage = postgres.getDockerImageName();

            // Log for debugging
            logger.info("JDBC URL: {}", jdbcUrl);
            logger.info("Resolved image: {}", resolvedImage);

            // Assert
            assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
            assertNotNull(jdbcUrl, "JDBC URL should not be null");
            assertTrue(jdbcUrl.contains("jdbc:postgresql://"), "JDBC URL should be a valid PostgreSQL URL");
            assertEquals(EXPECTED_IMAGE, resolvedImage, 
                "Docker image should be substituted to " + EXPECTED_IMAGE);
        } catch (Exception e) {
            logger.error("Failed to start container", e);
            fail("Container startup failed unexpectedly: " + e.getMessage());
        }
    }
}