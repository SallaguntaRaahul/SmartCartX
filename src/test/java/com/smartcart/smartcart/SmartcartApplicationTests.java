package com.smartcart.smartcart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers=localhost:9092",
		"spring.kafka.properties.security.protocol=PLAINTEXT",
		"spring.data.redis.host=localhost",
		"spring.data.redis.port=6379",
		"spring.cache.type=simple"
})
@DisplayName("Application Context Tests")
class SmartcartApplicationTests {

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
	}
}