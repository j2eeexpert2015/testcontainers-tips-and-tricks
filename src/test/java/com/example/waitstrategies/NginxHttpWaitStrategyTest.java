package com.example.waitstrategies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class NginxHttpWaitStrategyTest {

    private static final Logger logger = LoggerFactory.getLogger(NginxHttpWaitStrategyTest.class);

    @Container
    private static final GenericContainer<?> nginx = new GenericContainer<>("nginx:latest")
            .withExposedPorts(80)
            .waitingFor(Wait.forHttp("/")
                    .forStatusCode(200));

    @Test
    void testNginxIsReady() throws IOException {
        String address = "http://" + nginx.getHost() + ":" + nginx.getMappedPort(80);
        logger.info("Nginx is available at {}", address);

        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode, "Nginx should respond with HTTP 200");
        logger.info("Nginx responded with status: {}", responseCode);
    }
}
