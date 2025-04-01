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

/**
 * Integration test class for MySQL database initialization using file copying.
 * 
 * <p>This test demonstrates how to use Testcontainers with MySQL while copying SQL files
 * from classpath resources into the container's initialization directory. The test
 * validates both schema creation and data loading from the copied files.</p>
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Uses file copying ({@code withCopyFileToContainer}) from classpath resources</li>
 *   <li>Tests schema initialization (DDL) from copied schema.sql file</li>
 *   <li>Verifies test data loading (DML) from copied data.sql file</li>
 *   <li>Validates MySQL-specific features (temporal functions)</li>
 *   <li>Maintains identical test structure to PostgreSQL version for consistency</li>
 * </ul>
 * 
 * <p><b>File Structure:</b>
 * <pre>
 * src/test/resources/mysql/
 *   ├── schema.sql (DDL - table creation)
 *   └── data.sql  (DML - test data insertion)
 * </pre>
 * 
 * @see org.testcontainers.containers.MySQLContainer
 * @see org.testcontainers.utility.MountableFile
 * @since 1.0
 */
@Testcontainers
public class MysqlWithFileCopyTest {

    /**
     * MySQL test container configured with copied initialization scripts.
     * 
     * <p>The container:
     * <ul>
     *   <li>Uses MySQL 8.0 image</li>
     *   <li>Copies schema.sql to /docker-entrypoint-initdb.d/01-schema.sql</li>
     *   <li>Copies data.sql to /docker-entrypoint-initdb.d/02-data.sql</li>
     *   <li>Automatically starts before tests and stops after</li>
     * </ul>
     */
    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("mysql/schema.sql"),
            "/docker-entrypoint-initdb.d/01-schema.sql")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("mysql/data.sql"),
            "/docker-entrypoint-initdb.d/02-data.sql");

    /**
     * Tests that the MySQL container was properly initialized with schema and data
     * from the copied SQL files.
     * 
     * <p><b>Test validations include:</b>
     * <ol>
     *   <li>Schema validation - verifies all expected tables exist</li>
     *   <li>Data validation - checks record counts and values</li>
     *   <li>MySQL-specific features - tests temporal functions</li>
     *   <li>Relationship validation - verifies JOIN operations work</li>
     * </ol>
     * 
     * @throws Exception if any database operation fails
     */
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