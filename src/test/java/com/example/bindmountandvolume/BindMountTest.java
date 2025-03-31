package com.example.bindmountandvolume;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class BindMountTest {

    private static final Logger logger = LoggerFactory.getLogger(BindMountTest.class);

    private static final String HOST_DIR = "C:\\testcontainers_demo";
    private static final String HOST_FILE = HOST_DIR + "\\demo_file.txt";
    private static final String CONTAINER_FILE = "/data/demo_file.txt";

    @BeforeAll
    static void setupHostDirectory() throws IOException {
        Files.createDirectories(Paths.get(HOST_DIR));
        logger.info("Created host directory: {}", HOST_DIR);
    }

    @AfterAll
    static void cleanupHostDirectory() throws IOException {
        Files.deleteIfExists(Paths.get(HOST_FILE));
        Files.deleteIfExists(Paths.get(HOST_DIR));
        logger.info("Cleaned up host directory");
    }

    @Test
    void testBindMountFileSharing() throws IOException, InterruptedException {
        Files.writeString(Paths.get(HOST_FILE), "Hello from Windows!");
        logger.info("Created file on host: {}", HOST_FILE);

        try (GenericContainer<?> container = new GenericContainer<>("alpine:latest")
                .withCommand("sh", "-c", "echo 'Container ready' && tail -f /dev/null")
                .withFileSystemBind(HOST_DIR, "/data")
                .waitingFor(Wait.forLogMessage(".*Container ready.*", 1))
                .withStartupTimeout(Duration.ofSeconds(30))) {

            container.start();
            logger.info("Container started with bind mount");

            var execResult = container.execInContainer("cat", CONTAINER_FILE);
            assertEquals("Hello from Windows!", execResult.getStdout().trim());
            logger.info("Verified file content in container");

            container.execInContainer("sh", "-c", "echo 'Modified in container!' > " + CONTAINER_FILE);
            logger.info("Modified file in container");

            String hostContent = Files.readString(Paths.get(HOST_FILE));
            assertTrue(hostContent.contains("Modified in container!"));
            logger.info("Verified changes on host");
        }
    }
}
