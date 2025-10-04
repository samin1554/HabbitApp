package com.habbitLoggingService.habbitLoggingService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/api/users")  // User service
                .build();
    }

    @Bean
    public WebClient activityServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082/api/habits")  // Activity service
                .build();
    }
}