package com.example.waitstrategies;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Uses Wait.forHealthcheck() on a custom Redis image that defines a HEALTHCHECK.
 */
@Testcontainers
public class RedisHealthCheckWaitStrategyTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthCheckWaitStrategyTest.class);

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine-healthcheck")
            .withExposedPorts(6379)
            .waitingFor(
                    Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(120))
            );

    @Test
    void shouldStartRedisUsingHealthCheck() {
        String address = redis.getHost() + ":" + redis.getMappedPort(6379);
        logger.info("✅ Redis is running at {}", address);

        assertTrue(redis.isRunning(), "Redis container should be running and healthy");
        logger.info("✅ Redis container passed health check and is running.");
    }
}
