package com.example.logging;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.OutputFrame;

public class ReadSelectiveMySQLLogs {
    public static void main(String[] args) {
        try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
                .withCommand("sh", "-c", "echo Hello STDOUT && echo Hello STDERR 1>&2")) {
            mysql.start();

            String stdout = mysql.getLogs(OutputFrame.OutputType.STDOUT);
            String stderr = mysql.getLogs(OutputFrame.OutputType.STDERR);

            System.out.println("=== STDOUT ===\n" + stdout);
            System.out.println("=== STDERR ===\n" + stderr);
        }
    }
}
