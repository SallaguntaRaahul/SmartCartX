package com.smartcart.smartcart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@EmbeddedKafka(
		partitions = 1,
		brokerProperties = {
				"listeners=PLAINTEXT://localhost:9093",
				"port=9093"
		})
@TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers=localhost:9093",
		"spring.kafka.properties.security.protocol=PLAINTEXT",
		"spring.data.redis.host=localhost",
		"spring.data.redis.port=6379",
		"spring.cache.type=simple"
})
@DirtiesContext
@DisplayName("Application Context Tests")
class SmartcartApplicationTests {

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
	}
}