package com.example.containerstart;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SequentialContainerTest {

    private static final Logger logger = LoggerFactory.getLogger(SequentialContainerTest.class);

    private static PostgreSQLContainer<?> postgresContainer;
    private static GenericContainer<?> redisContainer;

    @BeforeAll
    static void startContainers() {
        long startTime = System.currentTimeMillis();

        postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        postgresContainer.start();
        logger.info("Postgres started");

        redisContainer = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379);
        redisContainer.start();
        logger.info("Redis started");

        long endTime = System.currentTimeMillis();
        logger.info("Total sequential startup time: {} ms", (endTime - startTime));
    }

    @AfterAll
    static void stopContainers() {
        postgresContainer.stop();
        redisContainer.stop();
    }

    @Test
    void containersRunning() {
        assertTrue(postgresContainer.isRunning());
        assertTrue(redisContainer.isRunning());
    }
}
