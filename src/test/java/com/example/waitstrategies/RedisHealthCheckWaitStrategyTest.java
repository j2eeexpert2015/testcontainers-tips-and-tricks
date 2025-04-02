package com.example.waitstrategies;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Demonstrates the usage of Wait.forHealthcheck() strategy.
 * 
 * Redis official image (redis:7-alpine) defines a Docker HEALTHCHECK instruction.
 * This strategy will wait until the health status of the container becomes "healthy".
 */
@Testcontainers
public class RedisHealthCheckWaitStrategyTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthCheckWaitStrategyTest.class);

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .waitingFor(Wait.forHealthcheck()) ;// Readiness check: Relies on Dockerfile's HEALTHCHECK

    /**
     * Test verifies that Redis container is healthy and ready before proceeding.
     * 
     * If the health check fails, the container will not start and test will fail.
     */
    @Test
    void testRedisHealthCheckStrategy() {
        String redisAddress = redis.getHost() + ":" + redis.getMappedPort(6379);
        logger.info("✅ Redis is running at {}", redisAddress);

        assertTrue(redis.isRunning(), "Redis container should be running and healthy");
        logger.info("✅ Redis container is healthy and running.");
    }
}
