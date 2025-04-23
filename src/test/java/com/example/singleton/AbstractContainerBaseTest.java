package com.example.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

// Base class using Singleton Pattern for MySQL
public abstract class AbstractContainerBaseTest {

    // Logger for this class
    private static final Logger logger = LoggerFactory.getLogger(AbstractContainerBaseTest.class);

    // Define the container specification
    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0");

    // Declare the container instance as static and final
    static final MySQLContainer<?> MY_SQL_CONTAINER;

    static {
        // Static initializer block: runs only ONCE per JVM classloading
        logger.info(" Static Initializer: Creating MySQL container instance...");
        MY_SQL_CONTAINER = new MySQLContainer<>(MYSQL_IMAGE)
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

        logger.info("Static Initializer: Starting MySQL container (this may take a moment)...");
        MY_SQL_CONTAINER.start(); // Start the container *once*
        logger.info("Static Initializer: MySQL container started successfully on host: {} and port: {}",
                MY_SQL_CONTAINER.getHost(), MY_SQL_CONTAINER.getMappedPort(3306));

        // Ryuk handles cleanup, but a shutdown hook can log explicit stop intention (optional)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info(">>> JVM Shutdown Hook: Test suite finished. Ryuk should stop the container shortly.");
            // No need to call MY_SQL_CONTAINER.stop() here, Ryuk manages it.
        }));
    }

    // Convenience method for subclasses
    protected static String getJdbcUrl() {
        return MY_SQL_CONTAINER.getJdbcUrl();
    }
}