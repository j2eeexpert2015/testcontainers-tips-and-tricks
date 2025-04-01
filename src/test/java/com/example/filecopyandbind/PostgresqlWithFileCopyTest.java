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

/**
 * Integration test class for PostgreSQL database initialization using file copying.
 * 
 * <p>This test demonstrates how to use Testcontainers with PostgreSQL while copying SQL files
 * from classpath resources into the container's initialization directory. The test
 * validates both schema creation and data loading from the copied files.</p>
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Uses file copying ({@code withCopyFileToContainer}) from classpath resources</li>
 *   <li>Tests schema initialization (DDL) from copied schema.sql file</li>
 *   <li>Verifies test data loading (DML) from copied data.sql file</li>
 *   <li>Validates PostgreSQL-specific features (JSONB support)</li>
 *   <li>Maintains identical test structure to file binding version for consistency</li>
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
 * @see org.testcontainers.utility.MountableFile
 * @since 1.0
 */
@Testcontainers
public class PostgresqlWithFileCopyTest {

    /**
     * PostgreSQL test container configured with copied initialization scripts.
     * 
     * <p>The container:
     * <ul>
     *   <li>Uses PostgreSQL 16 Alpine image</li>
     *   <li>Copies schema.sql to /docker-entrypoint-initdb.d/01-schema.sql</li>
     *   <li>Copies data.sql to /docker-entrypoint-initdb.d/02-data.sql</li>
     *   <li>Automatically starts before tests and stops after</li>
     * </ul>
     */
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("postgres/schema.sql"),
            "/docker-entrypoint-initdb.d/01-schema.sql")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("postgres/data.sql"),
            "/docker-entrypoint-initdb.d/02-data.sql");

    /**
     * Tests that the PostgreSQL container was properly initialized with schema and data
     * from the copied SQL files.
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