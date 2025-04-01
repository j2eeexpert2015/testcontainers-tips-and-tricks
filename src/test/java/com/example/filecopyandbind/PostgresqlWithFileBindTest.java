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

/**
 * Integration test class for PostgreSQL database initialization using file system binding.
 * 
 * <p>This test demonstrates how to use Testcontainers with PostgreSQL while mounting SQL files
 * directly from the host filesystem into the container's initialization directory. The test
 * validates both schema creation and data loading from the mounted files.</p>
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Uses file system binding ({@code withFileSystemBind}) instead of file copying</li>
 *   <li>Tests schema initialization (DDL) from mounted schema.sql file</li>
 *   <li>Verifies test data loading (DML) from mounted data.sql file</li>
 *   <li>Validates PostgreSQL-specific features (JSONB support)</li>
 *   <li>Tests relational integrity between tables</li>
 * </ul>
 * 
 * <p><b>File Structure:</b>
 * <pre>
 * src/test/resources/postgres/
 *   ├── schema.sql (DDL - table creation)
 *   └── data.sql  (DML - test data insertion)
 * </pre>
 * 
 * @see org.testcontainers.containers.PostgreSQLContainer
 * @see org.testcontainers.containers.BindMode
 * @since 1.0
 */
@Testcontainers
public class PostgresqlWithFileBindTest {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresqlWithFileBindTest.class);

    /**
     * PostgreSQL test container configured with file system binds for initialization scripts.
     * 
     * <p>The container:
     * <ul>
     *   <li>Uses PostgreSQL 16 Alpine image</li>
     *   <li>Mounts schema.sql as read-only to /docker-entrypoint-initdb.d/01-schema.sql</li>
     *   <li>Mounts data.sql as read-only to /docker-entrypoint-initdb.d/02-data.sql</li>
     *   <li>Automatically starts before tests and stops after</li>
     * </ul>
     */
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

    /**
     * Tests that the PostgreSQL container was properly initialized with schema and data
     * from the bound SQL files.
     * 
     * <p><b>Test validations include:</b>
     * <ol>
     *   <li>Schema validation - verifies all expected tables exist</li>
     *   <li>Data validation - checks record counts and values</li>
     *   <li>PostgreSQL-specific features - tests JSONB functionality</li>
     *   <li>Relationship validation - verifies JOIN operations work</li>
     * </ol>
     * 
     * @throws Exception if any database operation fails
     */
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