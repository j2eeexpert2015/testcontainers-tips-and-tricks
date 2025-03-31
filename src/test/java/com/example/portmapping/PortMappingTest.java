package com.example.portmapping;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.ExposedPort;

@Testcontainers
public class PortMappingTest {
	private static final Logger logger = LoggerFactory.getLogger(PortMappingTest.class);

	// ---------------------------
	// Scenario 1: Dynamic Port Mapping (Recommended)
	// ---------------------------
	@Container
	PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");

	@Container
	KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.0"));

	@Container
	MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	// ---------------------------
	// Scenario 2: Fixed Port Mapping (Use with caution)
	// ---------------------------
	@Container
	PostgreSQLContainer<?> postgresFixed = new PostgreSQLContainer<>("postgres:13")
			.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(cmd.getHostConfig()
					.withPortBindings(new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432)))));

	@Test
	void demonstratePortMappingStrategies() {
		logger.info("\n\n===== Dynamic Port Allocation (Recommended for Tests) =====");
		logContainerPorts("PostgreSQL", postgres);
		logContainerPorts("Kafka", kafka);
		logContainerPorts("MySQL", mysql);

		logger.info("\n\n===== Fixed Port Mapping (Use with Caution!) =====");
		logger.info("PostgreSQL (Fixed): localhost:{} → container:5432", postgresFixed.getMappedPort(5432));

		//verifyConnections();
	}

	private void logContainerPorts(String name, GenericContainer<?> container) {
		logger.info("{}\n- Exposed Ports: {}\n- Mapped Port: {}\n- Host: {}", name, container.getExposedPorts(),
				container.getFirstMappedPort(), container.getHost());
	}

	/*
	private void verifyConnections() {
		try {
			// Test PostgreSQL connection
			postgres.withDatabaseName("test").start();
			logger.info("\nPostgreSQL JDBC URL: {}", postgres.getJdbcUrl());

			// Test Kafka (requires advertised listeners)
			kafka.withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:" + kafka.getMappedPort(9092));
			logger.info("Kafka Bootstrap Servers: {}", kafka.getBootstrapServers());

			// Test MySQL
			logger.info("MySQL JDBC URL: {}", mysql.getJdbcUrl());

		} catch (Exception e) {
			logger.error("Connection test failed", e);
		}
	}
	*/
}
