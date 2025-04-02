package com.example.waitstrategies;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Testcontainers
public class ElasticsearchWaitStrategyTest {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchWaitStrategyTest.class);

	@Container
	private static final ElasticsearchContainer elasticsearch = new ElasticsearchContainer("elasticsearch:8.5.0")
			.withEnv("discovery.type", "single-node").withEnv("xpack.security.enabled", "false")
			.waitingFor(Wait.forHttp("/_cluster/health").forStatusCode(200)
					.forResponsePredicate(response -> response.contains("\"status\":\"green\"")))
			.withLogConsumer(new Slf4jLogConsumer(logger));

	@Test
	void testElasticsearchOperations() throws IOException {
		// Create the low-level REST client
		RestClient restClient = RestClient
				.builder(new HttpHost(elasticsearch.getHost(), elasticsearch.getMappedPort(9200))).build();

		// Create the transport and client
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		ElasticsearchClient client = new ElasticsearchClient(transport);

		// Create and index a document
		Product product = new Product("1", "Elasticsearch Guide", 29.99);
		IndexResponse response = client.index(i -> i.index("products").id(product.id()).document(product));

		logger.info("Indexed document with ID: {}", response.id());

		// Retrieve and verify the document
		Product retrieved = client.get(g -> g.index("products").id(product.id()), Product.class).source();

		assertThat(retrieved).isNotNull();
		assertThat(retrieved.name()).isEqualTo("Elasticsearch Guide");
	}

	record Product(String id, String name, double price) {
	}
}