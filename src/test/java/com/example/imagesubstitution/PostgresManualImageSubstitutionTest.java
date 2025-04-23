package com.example.imagesubstitution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresManualImageSubstitutionTest {

	private static final Logger logger = LoggerFactory.getLogger(PostgresManualImageSubstitutionTest.class);

	// Constant for substituted image
	private static final String GHCR_POSTGRES_IMAGE = "ghcr.io/j2eeexpert2015/approved-images/postgres:15";

	@Test
	void validateManualSubstitutionWithDockerImageName() {
		// Define the custom image and mark it as compatible with "postgres"
		DockerImageName myImage = DockerImageName
				.parse(GHCR_POSTGRES_IMAGE)
				.asCompatibleSubstituteFor("postgres");

		try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(myImage)) {
			postgres.start();
			logger.info("JDBC URL (DockerImageName): {}", postgres.getJdbcUrl());
			String resolvedImage = postgres.getDockerImageName();
			logger.info("Resolved image (DockerImageName): {}", resolvedImage);
			assertTrue(postgres.isRunning(), "Postgres container should be running (DockerImageName)");
			assertEquals(GHCR_POSTGRES_IMAGE, resolvedImage,
					"Expected image from GHCR, but got: " + resolvedImage + " (DockerImageName)");
		} catch (Exception e) {
			logger.error("Failed to start container (DockerImageName)", e);
			throw e;
		}
	}
}
