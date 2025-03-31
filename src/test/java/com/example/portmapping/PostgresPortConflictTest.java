package com.example.portmapping;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresPortConflictTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresPortConflictTest.class);

    @Test
    void demonstratePostgreSQLContainerPortConflict() {
        try {
            PostgreSQLContainer<?> postgres1 = new PostgreSQLContainer<>("postgres:15")
                    .withCreateContainerCmdModifier(cmd ->
                            cmd.withHostConfig(cmd.getHostConfig().withPortBindings(
                                    new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432))
                            ))
                    );

            postgres1.start();
            logger.info("First PostgreSQLContainer started at {}:{}", postgres1.getHost(), postgres1.getMappedPort(5432));
            logger.info("JDBC URL 1: {}", postgres1.getJdbcUrl());

            PostgreSQLContainer<?> postgres2 = new PostgreSQLContainer<>("postgres:15")
                    .withCreateContainerCmdModifier(cmd ->
                            cmd.withHostConfig(cmd.getHostConfig().withPortBindings(
                                    new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432))
                            ))
                    );

            logger.info("🚀 Attempting to start second PostgreSQLContainer...");
            postgres2.start(); // This will fail due to port conflict

        } catch (Exception e) {
            logger.error("❌ Port conflict occurred while starting second PostgreSQLContainer", e);
        }
    }
}
