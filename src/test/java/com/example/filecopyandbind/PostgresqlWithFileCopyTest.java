package com.example.filecopyandbind;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Integration test for PostgreSQL initialization using file copying from classpath.
 * Copies schema.sql and data.sql into the container initialization directory.
 */
@Testcontainers
public class PostgresqlWithFileCopyTest {

    // PostgreSQL container configured with copied initialization scripts.
    // Uses PostgreSQL 16, copies schema and data files from classpath resources.
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("postgres/schema.sql"),
                    "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("postgres/data.sql"),
                    "/docker-entrypoint-initdb.d/02-data.sql");

    // Tests that the PostgreSQL container initialized correctly from copied SQL files.
    // Validates schema creation, data loading, and specific PostgreSQL features (JSONB).
    @Test
    public void testPostgresqlSchemaAndDataLoaded() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             var stmt = conn.createStatement()) {

            // ===== SCHEMA VALIDATION =====
            ResultSet rs = stmt.executeQuery("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_name IN ('customers', 'orders', 'json_data')
                """);
            assertTrue(rs.next(), "Should return at least one row");
            assertEquals(3, rs.getInt(1), "Should have created 3 tables");

            // ===== DATA VALIDATION =====
            rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            assertTrue(rs.next(), "Customers table should have rows");
            assertEquals(2, rs.getInt(1), "Should have 2 customer records");

            // ===== POSTGRES-SPECIFIC FEATURE TEST =====
            rs = stmt.executeQuery("SELECT data->>'system' FROM json_data LIMIT 1");
            assertTrue(rs.next(), "Should have JSON data");
            assertEquals("PostgreSQL", rs.getString(1), "Should have correct system value");

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