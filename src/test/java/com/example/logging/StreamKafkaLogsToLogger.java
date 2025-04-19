package com.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

public class StreamKafkaLogsToLogger {
    private static final Logger logger = LoggerFactory.getLogger(StreamKafkaLogsToLogger.class);

    public static void main(String[] args) {
        KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
                .withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams());

        kafka.start();
        logger.info("Kafka bootstrap servers: {}", kafka.getBootstrapServers());
        kafka.stop();
    }
}
