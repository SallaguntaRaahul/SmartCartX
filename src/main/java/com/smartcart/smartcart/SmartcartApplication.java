package com.smartcart.smartcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartcartApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartcartApplication.class, args);
	}
}