package com.example.waitstrategies;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class KafkaWaitStrategyTest {
	private static final Logger logger = LoggerFactory.getLogger(KafkaWaitStrategyTest.class);
	
	/*
	 * Currently, if you chain multiple waitingFor() calls - which means the last one will override the previous ones
	 */
	
    
	@Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.0.0"))
        // Liveness check
        .waitingFor(Wait.forListeningPort())
        // Readiness check
    	//.waitingFor(Wait.forLogMessage(".*Kafka Server.*started.*", 1))
        .waitingFor(Wait.forLogMessage(".*KafkaServer id=.* started.*", 1))
    		.withLogConsumer(new Slf4jLogConsumer(logger));  
    
    

    @Test
    void testKafkaOperations() throws ExecutionException, InterruptedException {
    	String topicName = "test-topic";
    	// 1. Create topic
        createTopic(topicName);

        // 2. Verify topic exists with assertion
        assertTrue(topicExists(topicName), "Topic should exist after creation");
        
        logger.info("✅ Kafka topic '{}' is created and available.", topicName);

        
    }

    private void createTopic(String topicName) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            admin.createTopics(Collections.singletonList(newTopic)).all().get();
        }
    }

    private boolean topicExists(String topicName) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(props)) {
            return admin.listTopics().names().get().contains(topicName);
        }
    }

   
}