package com.example.bindmountandvolume;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresVolumePersistenceTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresVolumePersistenceTest.class);

    private static final String DB_NAME = "testdb";
    private static final String DB_USER = "testuser";
    private static final String DB_PASSWORD = "testpass";

    private static final String HOST_VOLUME_PATH = Paths.get(System.getProperty("user.home"), "postgres-data").toString();

    private static PostgreSQLContainer<?> postgresContainer;

    @BeforeAll
    static void startPostgresContainer() {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD)
                .withFileSystemBind(HOST_VOLUME_PATH, "/var/lib/postgresql/data")
                .withReuse(true); // Optional - reuse in same JVM

        postgresContainer.start();
        logger.info("PostgreSQL container started with volume bind: {}", HOST_VOLUME_PATH);
    }

    @AfterAll
    static void stopPostgresContainer() {
        if (postgresContainer != null) {
            postgresContainer.stop();
            logger.info("PostgreSQL container stopped");
        }
    }

    @Test
    @Order(1)
    void testCreateAndInsertData() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(50) NOT NULL)");
            stmt.execute("INSERT INTO users (name) VALUES ('Alice')");
            logger.info("Inserted data into users table");
        }
    }

    @Test
    @Order(2)
    void testDataPersistenceAfterRestart() throws Exception {
        // Stop & restart the container to simulate restart
        postgresContainer.stop();
        postgresContainer.start();
        logger.info("PostgreSQL container restarted");

        try (Connection conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM users")) {

            assertTrue(rs.next(), "User data should exist after restart");
            assertEquals("Alice", rs.getString(1));
            logger.info("Verified data persistence after restart");
        }
    }
}
