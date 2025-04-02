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

	@Test
	void validateManualSubstitutionWithDockerImageName() {
		// Define the custom image and mark it as compatible with "postgres"
		DockerImageName myImage = DockerImageName.parse("ghcr.io/mrayandutta/postgres-demo/postgres:15")
				.asCompatibleSubstituteFor("postgres");

		try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(myImage).withPassword("mysecretpassword")) {
			postgres.start();
			logger.info("JDBC URL (DockerImageName): {}", postgres.getJdbcUrl());
			String resolvedImage = postgres.getDockerImageName();
			logger.info("Resolved image (DockerImageName): {}", resolvedImage);
			assertTrue(postgres.isRunning(), "Postgres container should be running (DockerImageName)");
			assertEquals("ghcr.io/mrayandutta/postgres-demo/postgres:15", resolvedImage,
					"Expected image from GHCR, but got: " + resolvedImage + " (DockerImageName)");
		} catch (Exception e) {
			logger.error("Failed to start container (DockerImageName)", e);
			throw e;
		}
	}

}