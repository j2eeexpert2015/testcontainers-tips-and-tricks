package com.example.logging;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.concurrent.TimeUnit;

public class WaitForPostgresLogMessage {
    public static void main(String[] args) throws Exception {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withCommand("sh", "-c", "echo Booting && sleep 2 && echo READY")) {

            WaitingConsumer consumer = new WaitingConsumer();
            postgres.followOutput(consumer, OutputFrame.OutputType.STDOUT);
            postgres.start();

            consumer.waitUntil(frame -> frame.getUtf8String().contains("READY"), 10, TimeUnit.SECONDS);
            System.out.println("PostgreSQL is ready!");
        }
    }
}