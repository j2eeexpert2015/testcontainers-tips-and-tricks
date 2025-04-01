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

/**
 * Integration test class for MySQL database initialization using file system binding.
 * 
 * <p>This test demonstrates how to use Testcontainers with MySQL while mounting SQL files
 * directly from the host filesystem into the container's initialization directory. The test
 * validates both schema creation and data loading from the mounted files.</p>
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Uses file system binding ({@code withFileSystemBind}) instead of file copying</li>
 *   <li>Tests schema initialization (DDL) from mounted schema.sql file</li>
 *   <li>Verifies test data loading (DML) from mounted data.sql file</li>
 *   <li>Validates MySQL-specific features (temporal functions)</li>
 *   <li>Maintains identical test structure to file copy version for consistency</li>
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
 * @see org.testcontainers.containers.BindMode
 * @since 1.0
 */
@Testcontainers
public class MysqlWithFileBindTest {

    /**
     * MySQL test container configured with file system binds for initialization scripts.
     * 
     * <p>The container:
     * <ul>
     *   <li>Uses MySQL 8.0 image</li>
     *   <li>Mounts schema.sql as read-only to /docker-entrypoint-initdb.d/01-schema.sql</li>
     *   <li>Mounts data.sql as read-only to /docker-entrypoint-initdb.d/02-data.sql</li>
     *   <li>Automatically starts before tests and stops after</li>
     * </ul>
     */
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

    /**
     * Tests that the MySQL container was properly initialized with schema and data
     * from the bound SQL files.
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