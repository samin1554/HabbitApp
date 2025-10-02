package com.habbitactivityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        log.info("Validating userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            log.warn("Invalid userId: null or empty");
            return false;
        }

        try {
            Boolean result = userServiceWebClient.get()
                    .uri("/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            log.info("User validation result for userId {}: {}", userId, result);
            return result != null && result;

        } catch (WebClientResponseException e) {
            log.error("WebClient error validating userId: {}, Status: {}, Response: {}", 
                     userId, e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found: {}", userId);
                return false;
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.warn("Invalid userId format: {}", userId);
                return false;
            }
            log.warn("Other HTTP error during validation: {}", e.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error validating userId: " + userId, e);
            return false;
        }
    }
}