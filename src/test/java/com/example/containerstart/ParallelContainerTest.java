package com.example.containerstart;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelContainerTest {

    private static final Logger logger = LoggerFactory.getLogger(ParallelContainerTest.class);

    private static PostgreSQLContainer<?> postgresContainer;
    private static GenericContainer<?> redisContainer;

    @BeforeAll
    static void startContainers() {
        postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

        redisContainer = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379);

        long startTime = System.currentTimeMillis();

        Startables.deepStart(Stream.of(postgresContainer, redisContainer)).join();

        long endTime = System.currentTimeMillis();
        logger.info("Total parallel startup time: {} ms", (endTime - startTime));
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

