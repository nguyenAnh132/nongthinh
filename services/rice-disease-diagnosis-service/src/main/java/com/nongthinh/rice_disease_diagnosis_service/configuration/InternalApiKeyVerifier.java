package com.nongthinh.rice_disease_diagnosis_service.configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InternalApiKeyVerifier {

    private final String currentServiceApiKey;

    public InternalApiKeyVerifier(
            @Value("${spring.security.api-key.current-service-key}") String currentServiceApiKey) {
        this.currentServiceApiKey = currentServiceApiKey;
    }

    public void verify(String apiKey) {
        if (apiKey == null || currentServiceApiKey == null
                || !MessageDigest.isEqual(
                        currentServiceApiKey.getBytes(StandardCharsets.UTF_8),
                        apiKey.getBytes(StandardCharsets.UTF_8))) {
            log.warn("Rejected internal diagnosis request because its API key is invalid");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
    }
}
