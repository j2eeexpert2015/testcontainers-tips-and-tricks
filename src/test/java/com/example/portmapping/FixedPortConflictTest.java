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
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class FixedPortConflictTest {
    private static final Logger logger = LoggerFactory.getLogger(FixedPortConflictTest.class);

    @Test
    void demonstratePortConflict() {
        try {
            GenericContainer<?> firstContainer = new GenericContainer<>(DockerImageName.parse("postgres:15"))
                    .withCreateContainerCmdModifier(cmd -> 
                        cmd.withHostConfig(cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432))
                        ))
                    )
                    .waitingFor(Wait.forLogMessage(".*database system is ready.*", 1));

            firstContainer.start();
            logger.info("\n✅ First container running on port 15432");

            GenericContainer<?> conflictingContainer = new GenericContainer<>(DockerImageName.parse("postgres:15"))
                    .withCreateContainerCmdModifier(cmd -> 
                        cmd.withHostConfig(cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432))
                        ))
                    );

            logger.info("\n Attempting to start conflicting container...");
            conflictingContainer.start(); // This will fail

        } catch (Exception e) {
            logger.error("\n Port conflict occurred!", e);
            logger.info("\n Solution: Use PostgreSQLContainer with dynamic ports:");
            showPostgresqlSolution();
        }
    }

    void showPostgresqlSolution() {
        try (PostgreSQLContainer<?> postgres1 = new PostgreSQLContainer<>("postgres:15");
             PostgreSQLContainer<?> postgres2 = new PostgreSQLContainer<>("postgres:15")) {

            postgres1.start();
            postgres2.start();

            logger.info("\n🔹 PostgreSQL 1: {}", postgres1.getJdbcUrl());
            logger.info("🔹 PostgreSQL 2: {}", postgres2.getJdbcUrl());
            logger.info("\nNote how they get different ports automatically:");
            logger.info("Postgres1 port: {}", postgres1.getMappedPort(5432));
            logger.info("Postgres2 port: {}", postgres2.getMappedPort(5432));
        }
    }
}
