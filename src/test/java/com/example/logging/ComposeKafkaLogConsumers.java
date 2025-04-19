package com.example.logging;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ComposeKafkaLogConsumers {
    public static void main(String[] args) throws Exception {
        KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
                .withCommand("sh", "-c", "echo Init && sleep 1 && echo STARTED");

        ToStringConsumer stringConsumer = new ToStringConsumer();
        WaitingConsumer waitingConsumer = new WaitingConsumer();

        Consumer<OutputFrame> composed = stringConsumer.andThen(waitingConsumer);
        kafka.followOutput(composed);
        kafka.start();

        waitingConsumer.waitUntil(frame -> frame.getUtf8String().contains("STARTED"), 10, TimeUnit.SECONDS);
        System.out.println("Kafka Container Logs:\n" + stringConsumer.toUtf8String());
        kafka.stop();
    }
}
