package com.example.environmentvariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * This class demonstrates and tests the effectiveness of environment variables
 * in Docker containers, particularly focusing on PostgreSQL containers. It uses
 * Testcontainers to spin up temporary containers for testing purposes.
 */
@Testcontainers
public class EnvironmentVariableTest {

	// Logger for outputting test information
	private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariableTest.class);

	/**
	 * Tests basic environment variable functionality in a generic Alpine Linux
	 * container. This test verifies that an environment variable set in the
	 * container is accessible and can be printed to the container's logs.
	 */
	@Test
	void testContainerWithEnvVariable() {
		// Create an Alpine container with a custom environment variable and command
		try (GenericContainer<?> container = new GenericContainer<>("alpine:latest")
				.withEnv("MY_ENV_VAR", "HelloWorld")
				.withCommand("sh", "-c", "echo $MY_ENV_VAR && sleep 5")) {

			container.start();

			// Get container logs
			String logs = container.getLogs();
			System.out.println("Container Logs: " + logs);

			// Verify the environment variable was properly set and printed
			assertEquals(true, logs.contains("HelloWorld"));
		}
	}

	/**
	 * Tests PostgreSQL container configuration using individual environment
	 * variables. Verifies that PostgreSQL-specific environment variables are
	 * properly set in the container by checking the container's environment
	 * directly.
	 * 
	 * @throws Exception if container operations fail
	 */
	@Test
	void testPostgresContainerWithEnvVariables() throws Exception {
		logger.info("Starting testPostgresContainerWithEnvVariables using withEnv(key, value)");

		// Create PostgreSQL container with individual environment variables
		try (GenericContainer<?> postgres = new GenericContainer<>(DockerImageName.parse("postgres:13"))
				.withEnv("POSTGRES_USER", "testuser").withEnv("POSTGRES_PASSWORD", "testpass")
				.withEnv("POSTGRES_DB", "testdb").withEnv("PGDATA", "/var/lib/postgresql/data/pgdata")
				.withExposedPorts(5432)) {

			postgres.start();

			// Retrieve environment variables directly from the container
			String db = postgres.execInContainer("printenv", "POSTGRES_DB").getStdout().trim();
			String user = postgres.execInContainer("printenv", "POSTGRES_USER").getStdout().trim();
			String pass = postgres.execInContainer("printenv", "POSTGRES_PASSWORD").getStdout().trim();
			String pgdata = postgres.execInContainer("printenv", "PGDATA").getStdout().trim();

			// Log retrieved values for debugging
			logger.info("POSTGRES_DB: {}", db);
			logger.info("POSTGRES_USER: {}", user);
			logger.info("POSTGRES_PASSWORD: {}", pass);
			logger.info("PGDATA: {}", pgdata);

			// Assert that environment variables match expected values
			assertEquals("testdb", db);
			assertEquals("testuser", user);
			assertEquals("testpass", pass);
			assertEquals("/var/lib/postgresql/data/pgdata", pgdata);
		}
	}

	/**
	 * Tests PostgreSQL container configuration using a Map of environment
	 * variables. Demonstrates an alternative approach to setting multiple
	 * environment variables and verifies they are properly set in the container.
	 * 
	 * @throws Exception if container operations fail
	 */
	@Test
	void testPostgresContainerWithEnvMap() throws Exception {
		logger.info("Starting testPostgresContainerWithEnvMap using withEnv(Map)");

		// Create PostgreSQL container with environment variables from a Map
		try (GenericContainer<?> postgres = new GenericContainer<>(DockerImageName.parse("postgres:13"))
				.withEnv(getDatabaseVars()).withEnv("PGDATA", "/var/lib/postgresql/data/pgdata")
				.withExposedPorts(5432)) {

			postgres.start();

			// Retrieve environment variables directly from the container
			String db = postgres.execInContainer("printenv", "POSTGRES_DB").getStdout().trim();
			String user = postgres.execInContainer("printenv", "POSTGRES_USER").getStdout().trim();
			String pass = postgres.execInContainer("printenv", "POSTGRES_PASSWORD").getStdout().trim();
			String pgdata = postgres.execInContainer("printenv", "PGDATA").getStdout().trim();

			// Log retrieved values for debugging
			logger.info("POSTGRES_DB: {}", db);
			logger.info("POSTGRES_USER: {}", user);
			logger.info("POSTGRES_PASSWORD: {}", pass);
			logger.info("PGDATA: {}", pgdata);

			// Assert that environment variables match expected values
			assertEquals("testdb", db);
			assertEquals("testuser", user);
			assertEquals("testpass", pass);
			assertEquals("/var/lib/postgresql/data/pgdata", pgdata);
		}
	}

	/**
	 * Provides a standard set of database environment variables as a Map. This
	 * method centralizes the configuration for reuse across multiple tests.
	 * 
	 * @return Map containing standard PostgreSQL environment variables
	 */
	public static Map<String, String> getDatabaseVars() {
		return Map.of("POSTGRES_USER", "testuser", "POSTGRES_PASSWORD", "testpass", "POSTGRES_DB", "testdb");
	}
}