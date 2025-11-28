package com.park_karo.vehicle.config; // ⬅️ MAKE SURE THIS MATCHES YOUR PACKAGE PATH

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // This tells Spring Boot to load this configuration automatically
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS configuration to all API endpoints
                .allowedOrigins("*") // ⚠️ Allows ALL origins (crucial for Flutter)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
