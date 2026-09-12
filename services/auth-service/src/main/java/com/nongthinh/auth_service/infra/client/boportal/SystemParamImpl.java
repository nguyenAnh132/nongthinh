package com.nongthinh.auth_service.infra.client.boportal;

import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.infra.client.boportal.dto.SystemParamValueResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("systemParamImpl")
@Slf4j
public class SystemParamImpl implements SystemParam {

    private final BoPortalClient boPortalClient;
    private final String apiKey;

    public SystemParamImpl(
            BoPortalClient boPortalClient,
            @Value("${spring.security.api-key.clients.bo-portal-service.api-key}") String apiKey) {
        this.boPortalClient = boPortalClient;
        this.apiKey = apiKey;
    }

    @Override
    public String getString(String name, String defaultValue) {
        return fetch(name).orElse(defaultValue);
    }

    @Override
    public int getInt(String name, int defaultValue) {

        long start = System.currentTimeMillis();

        Optional<String> raw = fetch(name);
        if (raw.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.get().trim());
        } catch (NumberFormatException ex) {
            log.warn(
                    "[INFRA - SystemParam] System param {} value '{}' is not an integer, using default {} durationMs={}",
                    name,
                    raw,
                    defaultValue,
                    System.currentTimeMillis() - start
            );
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return fetch(name)
                .map(value -> Boolean.parseBoolean(value.trim()))
                .orElse(defaultValue);
    }

    private Optional<String> fetch(String name) {
        long start = System.currentTimeMillis();
        try {
            ApiResponse<SystemParamValueResponse> response = boPortalClient.getSystemParam(name, apiKey);
            String value = extractValue(response);
            return Optional.ofNullable(value);
        } catch (Exception ex) {
            log.warn(
                    "[INFRA - SystemParam] Failed to fetch system param {} durationMs={}",
                    name,
                    System.currentTimeMillis() - start
            );
            return Optional.empty();
        }
    }

    private static String extractValue(ApiResponse<SystemParamValueResponse> response) {
        if (response == null || response.getResult() == null) {
            return null;
        }
        return response.getResult().value();
    }
}
