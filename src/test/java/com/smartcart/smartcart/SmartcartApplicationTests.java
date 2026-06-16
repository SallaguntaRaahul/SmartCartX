package com.smartcart.smartcart;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class SmartcartApplicationTests {

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
	}
}