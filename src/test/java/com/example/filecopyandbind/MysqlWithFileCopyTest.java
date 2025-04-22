package com.example.filecopyandbind;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Integration test for MySQL initialization using file copying from classpath.
 * Copies schema.sql and data.sql into the container initialization directory.
 */
@Testcontainers
public class MysqlWithFileCopyTest {

    // MySQL container configured with copied initialization scripts.
    // Uses MySQL 8.0, copies schema and data files from classpath resources.
    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mysql/schema.sql"),
                    "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mysql/data.sql"),
                    "/docker-entrypoint-initdb.d/02-data.sql");

    // Tests that the MySQL container initialized correctly from copied SQL files.
    // Validates schema creation, data loading, and specific MySQL features.
    @Test
    public void testMysqlSchemaAndDataLoaded() throws Exception {
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