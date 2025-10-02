package com.habbiteditingservice.habbiteditingservice.editingservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserValidationService {
    
    @Value("${user.service.url}")
    private String userServiceUrl;

    public boolean validateUser(String userId) {
        log.info("Validating userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            log.warn("Invalid userId: null or empty");
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = userServiceUrl + "/" + userId + "/validate";
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            log.info("User validation result for userId {}: {}", userId, response.getBody());
            return response.getBody() != null && response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("HTTP error validating userId: {}, Status: {}, Response: {}", 
                     userId, e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("User not found or invalid userId format: {}", userId);
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