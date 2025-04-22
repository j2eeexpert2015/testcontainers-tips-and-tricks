package com.example.portmapping;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresPortMappingStrategiesTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresPortMappingStrategiesTest.class);
    private static final int FIXED_HOST_PORT = 15432;
    private static final int POSTGRES_PORT = 5432;

    // 1. Dynamic Port Mapping (Recommended for CI and tests)
    @Test
    void dynamicPortMapping() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")) {

            postgres.start();

            logger.info("\n=== DYNAMIC PORT MAPPING ===");
            logConnectionDetails(postgres);
        }
    }

    // 2. Fixed Port Mapping using GenericContainer (for local dev/demo)
    @Test
    void fixedPortMappingWithGenericContainer() {
        try (GenericContainer<?> postgres = new GenericContainer<>("postgres:15")
                .withEnv("POSTGRES_USER", "testuser")
                .withEnv("POSTGRES_PASSWORD", "testpass")
                .withEnv("POSTGRES_DB", "testdb")
                .withCreateContainerCmdModifier(cmd ->
                        cmd.withHostConfig(
                                cmd.getHostConfig().withPortBindings(
                                        new PortBinding(
                                                Ports.Binding.bindPort(FIXED_HOST_PORT),
                                                new ExposedPort(POSTGRES_PORT)
                                        )
                                )
                        )
                )
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1))) {

            postgres.start();

            logger.info("\n=== FIXED PORT MAPPING (GenericContainer) ===");
            logger.info("Host port {} → Container port {}", FIXED_HOST_PORT, POSTGRES_PORT);
            logger.info("JDBC URL: jdbc:postgresql://{}:{}/testdb",
                    postgres.getHost(), postgres.getMappedPort(POSTGRES_PORT));
        }
    }

    // 3. Fixed Port Mapping using PostgreSQLContainer (advanced use case)
    @Test
    void fixedPortMappingWithPostgreSQLContainer() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withCreateContainerCmdModifier(cmd ->
                        cmd.withHostConfig(
                                cmd.getHostConfig().withPortBindings(
                                        new PortBinding(
                                                Ports.Binding.bindPort(FIXED_HOST_PORT),
                                                new ExposedPort(POSTGRES_PORT)
                                        )
                                )
                        ))) {

            postgres.start();

            logger.info("\n=== FIXED PORT MAPPING (PostgreSQLContainer with cmd modifier) ===");
            logConnectionDetails(postgres);
        }
    }

    // Helper method to log PostgreSQLContainer info
    private void logConnectionDetails(PostgreSQLContainer<?> postgres) {
        logger.info("Container ID: {}", postgres.getContainerId());
        logger.info("PostgreSQL port: {}", POSTGRES_PORT);
        logger.info("Mapped host port: {}", postgres.getMappedPort(POSTGRES_PORT));
        logger.info("JDBC URL: {}", postgres.getJdbcUrl());
        logger.info("Username: {}", postgres.getUsername());
        logger.info("Password: {}", postgres.getPassword());
    }
}
