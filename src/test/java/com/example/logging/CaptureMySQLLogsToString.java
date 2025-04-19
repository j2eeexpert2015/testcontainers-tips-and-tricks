package com.example.logging;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.OutputFrame.OutputType;

public class CaptureMySQLLogsToString {
    public static void main(String[] args) throws InterruptedException {
        try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
                .withCommand("sh", "-c", "echo Logging to string")) {

            ToStringConsumer consumer = new ToStringConsumer();
            mysql.followOutput(consumer, OutputType.STDOUT);
            mysql.start();

            Thread.sleep(1000); // allow logs to stream
            System.out.println("=== Captured Logs ===\n" + consumer.toUtf8String());
        }
    }
}
