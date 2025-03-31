package com.example.containerstart;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DependentContainerTest {

    private static final Logger logger = LoggerFactory.getLogger(DependentContainerTest.class);

    private static PostgreSQLContainer<?> postgresContainer;
    private static GenericContainer<?> appContainer;

    @BeforeAll
    static void startContainers() {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        postgresContainer.start();
        logger.info("Postgres started");

        appContainer = new GenericContainer<>(DockerImageName.parse("alpine:latest"))
                .withCommand("sh", "-c", "echo Application started after DB && sleep 30")
                .dependsOn(postgresContainer); // Dependency
        appContainer.start();
        logger.info("App container started after Postgres");
    }

    @AfterAll
    static void stopContainers() {
        postgresContainer.stop();
        appContainer.stop();
    }

    @Test
    void containersRunning() {
        assertTrue(postgresContainer.isRunning());
        assertTrue(appContainer.isRunning());
    }
}

