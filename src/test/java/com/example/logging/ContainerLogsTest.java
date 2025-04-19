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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerLogsTest {
    private static final Logger logger = LoggerFactory.getLogger(ContainerLogsTest.class);

    @Test
    void shouldCaptureFullPostgresLogs() {
        // Starts a PostgreSQL container and prints the entire log output
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
            postgres.start();

            String logs = postgres.getLogs();
            System.out.println("=== Full PostgreSQL Logs ===\n" + logs);

            // Basic check for known startup log line
            assertTrue(logs.contains("database system is ready to accept connections"),
                    "Expected PostgreSQL readiness log not found.");
        }
    }

    @Test
    void shouldReadSeparateStdOutAndStdErrLogs() {
        // Simulates separate STDOUT and STDERR output using Alpine-style shell command
        try (GenericContainer<?> container = new GenericContainer<>("alpine:latest")
                .withCommand("sh", "-c", "echo Hello STDOUT && echo Hello STDERR 1>&2")) {
            container.start();

            String stdout = container.getLogs(OutputFrame.OutputType.STDOUT);
            String stderr = container.getLogs(OutputFrame.OutputType.STDERR);

            System.out.println("=== STDOUT ===\n" + stdout);
            System.out.println("=== STDERR ===\n" + stderr);

            assertTrue(stdout.contains("Hello STDOUT"));
            assertTrue(stderr.contains("Hello STDERR"));
        }
    }

    @Test
    void shouldStreamPostgreSQLLogsToLogger() {
        // Streams PostgreSQL container logs directly to SLF4J logger
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams());

        postgres.start();
        logger.info("Postgres JDBC URL: {}", postgres.getJdbcUrl());
        postgres.stop();
    }

    @Test
    void captureMySQLLogsToString() throws InterruptedException {
        // Captures MySQL logs into a string buffer using ToStringConsumer
        MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");
        mysql.start();

        //LOGGER.debug(postgres.getLogs()); // prints startup logs

        ToStringConsumer consumer = new ToStringConsumer();
        mysql.followOutput(consumer, OutputFrame.OutputType.STDOUT);

        Thread.sleep(1000); // Give some time for logs to accumulate
        System.out.println("=== Captured Logs ===\n" + consumer.toUtf8String());
        mysql.stop();
    }

    @Test
    void waitForPostgresLogMessage() throws InterruptedException, TimeoutException {
        // Waits for a specific log message in PostgreSQL container logs using WaitingConsumer
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
        postgres.start();

        WaitingConsumer consumer = new WaitingConsumer();
        postgres.followOutput(consumer, OutputFrame.OutputType.STDOUT);

        consumer.waitUntil(frame -> frame.getUtf8String().contains("ready"), 30, TimeUnit.SECONDS);
        System.out.println("PostgreSQL is ready!");
    }

    @Test
    void composeKafkaLogConsumers() throws TimeoutException {
        // Streams Kafka logs to both SLF4J logger and in-memory string collector
        KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
                .withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams());

        ToStringConsumer stringConsumer = new ToStringConsumer();
        WaitingConsumer waitingConsumer = new WaitingConsumer();

        // Combine consumers to handle logs in parallel
        Consumer<OutputFrame> composed = stringConsumer.andThen(waitingConsumer);

        //kafka.followOutput(composed);
        kafka.start();
        kafka.followOutput(composed);

        waitingConsumer.waitUntil(
                frame -> frame.getUtf8String().contains("started (kafka.server.KafkaServer)"),
                60, TimeUnit.SECONDS
        );

        System.out.println("Kafka Container Logs:\n" + stringConsumer.toUtf8String());
        kafka.stop();
    }
}
