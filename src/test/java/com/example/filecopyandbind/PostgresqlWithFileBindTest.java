package com.example.filecopyandbind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 * Integration test for PostgreSQL initialization using file system binding (bind mounts).
 * Mounts schema.sql and data.sql from the host into the container initialization directory.
 */
@Testcontainers
public class PostgresqlWithFileBindTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresqlWithFileBindTest.class);

    // PostgreSQL container configured with file system binds for init scripts.
    // Uses PostgreSQL 16, mounts schema and data files as read-only.
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withFileSystemBind(
                    Paths.get("src/test/resources/postgres/schema.sql").toAbsolutePath().toString(),
                    "/docker-entrypoint-initdb.d/01-schema.sql",
                    BindMode.READ_ONLY)
            .withFileSystemBind(
                    Paths.get("src/test/resources/postgres/data.sql").toAbsolutePath().toString(),
                    "/docker-entrypoint-initdb.d/02-data.sql",
                    BindMode.READ_ONLY);

    // Tests that the PostgreSQL container initialized correctly from bound SQL files.
    // Validates schema creation, data loading, and specific PostgreSQL features (JSONB).
    @Test
    public void verifySchemaAndDataLoadedFromBindFiles() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             var stmt = conn.createStatement()) {

            // Schema validation
            ResultSet rs = stmt.executeQuery("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_name IN ('customers', 'orders', 'json_data')
                """);
            assertTrue(rs.next(), "Should return at least one row");
            assertEquals(3, rs.getInt(1), "Should have created 3 tables");

            // Data validation
            rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            assertTrue(rs.next(), "Customers table should have rows");
            assertEquals(2, rs.getInt(1), "Should have 2 customer records");

            rs = stmt.executeQuery("SELECT SUM(amount) FROM orders");
            assertTrue(rs.next(), "Orders table should have rows");
            assertTrue(rs.getDouble(1) > 0, "Order amounts should sum to positive value");

            // PostgreSQL-specific features
            rs = stmt.executeQuery("SELECT data->>'system' FROM json_data LIMIT 1");
            assertTrue(rs.next(), "Should have JSON data");
            assertEquals("PostgreSQL", rs.getString(1), "Should have correct system value");

            // Relationship validation
            rs = stmt.executeQuery("""
                SELECT c.name
                FROM customers c JOIN orders o ON c.id = o.customer_id
                WHERE o.amount > 100
                """);
            assertTrue(rs.next(), "Should find customer with order > 100");

            logger.info("All PostgreSQL bind mount validations passed successfully");
        }
    }
}