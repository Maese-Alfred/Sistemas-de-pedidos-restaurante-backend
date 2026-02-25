package com.restaurant.orderservice.config;

import com.restaurant.orderservice.security.KitchenSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration for the Order Service.
 * Allows the local frontend to call the API during development.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final KitchenSecurityInterceptor kitchenSecurityInterceptor;

    @Autowired
    public WebConfig(KitchenSecurityInterceptor kitchenSecurityInterceptor) {
        this.kitchenSecurityInterceptor = kitchenSecurityInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String patterns = System.getenv("CORS_ALLOWED_ORIGIN_PATTERNS");
        if (patterns != null && !patterns.isBlank()) {
            String[] allowedPatterns = patterns.split(",");
            for (int i = 0; i < allowedPatterns.length; i++) {
                allowedPatterns[i] = allowedPatterns[i].trim();
            }

            registry.addMapping("/**")
                    .allowedOriginPatterns(allowedPatterns)
                    .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
            return;
        }

        String origins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (origins == null || origins.isBlank()) {
            throw new IllegalStateException(
                "CORS_ALLOWED_ORIGINS or CORS_ALLOWED_ORIGIN_PATTERNS environment variable must be defined");
        }

        String[] allowedOrigins = origins.split(",");
        for (int i = 0; i < allowedOrigins.length; i++) {
            allowedOrigins[i] = allowedOrigins[i].trim();
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(kitchenSecurityInterceptor).addPathPatterns("/**");
    }
}
