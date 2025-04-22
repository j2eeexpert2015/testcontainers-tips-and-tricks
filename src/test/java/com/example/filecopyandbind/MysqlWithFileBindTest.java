package com.example.filecopyandbind;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Integration test for MySQL initialization using file system binding (bind mounts).
 * Mounts schema.sql and data.sql from the host into the container initialization directory.
 */
@Testcontainers
public class MysqlWithFileBindTest {

    // MySQL container configured with file system binds for init scripts.
    // Uses MySQL 8.0, mounts schema and data files as read-only.
    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withFileSystemBind(
                    Paths.get("src/test/resources/mysql/schema.sql").toAbsolutePath().toString(),
                    "/docker-entrypoint-initdb.d/01-schema.sql",
                    BindMode.READ_ONLY)
            .withFileSystemBind(
                    Paths.get("src/test/resources/mysql/data.sql").toAbsolutePath().toString(),
                    "/docker-entrypoint-initdb.d/02-data.sql",
                    BindMode.READ_ONLY);

    // Tests that the MySQL container initialized correctly from bound SQL files.
    // Validates schema creation, data loading, and specific MySQL features.
    @Test
    public void testMysqlWithLiveFileBinding() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword());
             var stmt = conn.createStatement()) {

            // ===== SCHEMA VALIDATION =====
            ResultSet rs = stmt.executeQuery("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_name IN ('customers', 'orders', 'temporal_data')
                """);
            assertTrue(rs.next(), "Should return at least one row");
            assertEquals(3, rs.getInt(1), "Should have created 3 tables");

            // ===== DATA VALIDATION =====
            rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            assertTrue(rs.next(), "Customers table should have rows");
            assertEquals(2, rs.getInt(1), "Should have 2 customer records");

            // ===== MYSQL-SPECIFIC FEATURE TEST =====
            rs = stmt.executeQuery("""
                SELECT TIMESTAMPDIFF(SECOND,
                    (SELECT event_time FROM temporal_data WHERE id = 1),
                    (SELECT event_time FROM temporal_data WHERE id = 2))
                """);
            assertTrue(rs.next(), "Should have temporal data");
            assertEquals(3600, rs.getInt(1), "Should show 1 hour difference");

            // ===== RELATIONSHIP TEST =====
            rs = stmt.executeQuery("""
                SELECT c.name
                FROM customers c JOIN orders o ON c.id = o.customer_id
                WHERE o.amount > 100
                """);
            assertTrue(rs.next(), "Should find customer with order > 100");
        }
    }
}