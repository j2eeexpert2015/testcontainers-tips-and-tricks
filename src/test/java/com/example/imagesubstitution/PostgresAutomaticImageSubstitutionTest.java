package com.example.imagesubstitution;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresAutomaticImageSubstitutionTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresAutomaticImageSubstitutionTest.class);

    // Constant for image verification
    private static final String IMAGE_TAG = "15";
    private static final String EXPECTED_PREFIX = "ghcr.io/j2eeexpert2015/approved-images/";
    private static final String EXPECTED_IMAGE = EXPECTED_PREFIX + "postgres:" + IMAGE_TAG;

    @Test
    void validateExplicitSubstitutionWithExcludes() {
        DockerImageName imageName = DockerImageName.parse("postgres").withTag(IMAGE_TAG);

        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(imageName)) {
            postgres.start();

            String jdbcUrl = postgres.getJdbcUrl();
            String resolvedImage = postgres.getDockerImageName();

            logger.info("JDBC URL: {}", jdbcUrl);
            logger.info("Resolved image: {}", resolvedImage);

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
