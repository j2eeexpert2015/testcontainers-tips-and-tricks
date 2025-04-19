package com.example.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.fail;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerLogsTest {
    private static final Logger logger = LoggerFactory.getLogger(ContainerLogsTest.class);

    @Test
    void shouldCaptureFullPostgresLogs() {
        /*
            Using try-with-resources when managing containers manually, i.e. create/start/stop containers within a method (e.g., inside @Test)
            and want to ensure they’re cleaned up properly after the test finishes.
         */
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
            postgres.start();

            // Capture and print logs
            String logs = postgres.getLogs();
            System.out.println("=== Full PostgreSQL Logs ===\n" + logs);

            // Optional assertion (basic sanity check)
            assertTrue(logs.contains("database system is ready to accept connections"),
                    "Expected PostgreSQL readiness log not found.");
        }
    }

    @Test
    void shouldReadSeparateStdOutAndStdErrLogs() {
        try (GenericContainer<?> container = new GenericContainer<>("mysql:8")
                .withCommand("sh", "-c", "echo Hello STDOUT && echo Hello STDERR 1>&2")) {
            container.start();

            String stdout = container.getLogs(OutputFrame.OutputType.STDOUT);
            String stderr = container.getLogs(OutputFrame.OutputType.STDERR);
            //1>&2 redirects file descriptor 1 (STDOUT) to file descriptor 2 (STDERR).
            System.out.println("=== STDOUT ===\n" + stdout);
            System.out.println("=== STDERR ===\n" + stderr);

            // Assertions
            assertTrue(stdout.contains("Hello STDOUT"), "STDOUT should contain 'Hello STDOUT'");
            assertTrue(stderr.contains("Hello STDERR"), "STDERR should contain 'Hello STDERR'");
        }
    }

    @Test
    void shouldStreamPostgreSQLLogsToLogger() {
        /**
         * withSeparateOutputStreams() ensures:
         * STDOUT logs are tagged as STDOUT
         * STDERR logs are tagged as STDERR
         * This helps when you want to distinguish output types clearly in logs
         */
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15") //;
                .withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams());
        postgres.start();
        logger.info("Postgres Jdbc Url : {}", postgres.getJdbcUrl());
        postgres.stop();

    }

    @Test
    void captureMySQLLogsToString() throws InterruptedException {
        MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");
        mysql.start();

        ToStringConsumer consumer = new ToStringConsumer();
        mysql.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        Thread.sleep(1000); // allow logs to stream
        System.out.println("=== Captured Logs ===\n" + consumer.toUtf8String());
        mysql.stop();

    }

    @Test
    void waitForPostgresLogMessage() throws InterruptedException, TimeoutException {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
        postgres.start();
        WaitingConsumer consumer = new WaitingConsumer();
        postgres.followOutput(consumer, OutputFrame.OutputType.STDOUT);

        consumer.waitUntil(frame -> frame.getUtf8String().contains("ready"), 30, TimeUnit.SECONDS);
        System.out.println("PostgreSQL is ready!");

    }



    @Test
    void composeKafkaLogConsumers() throws TimeoutException {
        KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))//;
        .withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams());

        ToStringConsumer stringConsumer = new ToStringConsumer();
        WaitingConsumer waitingConsumer = new WaitingConsumer();

        Consumer<OutputFrame> composed = stringConsumer.andThen(waitingConsumer);
        kafka.start();
        kafka.followOutput(composed);

        waitingConsumer.waitUntil(frame -> frame.getUtf8String().contains("started (kafka.server.KafkaServer)"), 60, TimeUnit.SECONDS);
        System.out.println("Kafka Container Logs:\n" + stringConsumer.toUtf8String());
        kafka.stop();
    }
}
