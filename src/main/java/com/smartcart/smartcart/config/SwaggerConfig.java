package com.smartcart.smartcart.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI smartCartOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .description("Enter JWT token from /api/auth/login");

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("SmartCartX API")
                        .description(
                                "E-Commerce Backend built with " +
                                        "Spring Boot, Redis, Kafka, PostgreSQL")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Raahul Sallagunta")
                                .email("raahulsalla@gmail.com")))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                securityScheme));
    }
}