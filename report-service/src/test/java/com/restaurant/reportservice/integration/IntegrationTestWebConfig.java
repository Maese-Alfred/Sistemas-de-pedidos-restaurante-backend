package com.restaurant.reportservice.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Shared test configuration that replaces the production {@code WebConfig} bean.
 *
 * The production WebConfig reads CORS origins from environment variables,
 * which may not be available in test environments. This test config provides
 * sensible defaults for integration tests.
 *
 * Usage: add {@code @Import(IntegrationTestWebConfig.class)} to integration tests
 * that use {@code @SpringBootTest}.
 */
@TestConfiguration
public class IntegrationTestWebConfig {

    @Bean("webConfig")
    public WebMvcConfigurer webConfig() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
