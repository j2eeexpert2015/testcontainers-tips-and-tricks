package com.example.logging;

import org.testcontainers.containers.PostgreSQLContainer;

public class ReadFullPostgresLogs {
    public static void main(String[] args) {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withCommand("sh", "-c", "echo Starting PostgreSQL && sleep 1 && echo Ready")) {
            postgres.start();
            //Read Full Logs from PostgreSQL
            String logs = postgres.getLogs();
            System.out.println("=== Full PostgreSQL Logs ===\n" + logs);
        }
    }
}
