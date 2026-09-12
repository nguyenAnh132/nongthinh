package com.nongthinh.agri_catalog_service.application.service;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ValidationReportSanitizer {

    private static final int MAX_REPORT_LENGTH = 10_000;
    private final ObjectMapper objectMapper;

    public ValidationReportSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sanitize(String report) {
        if (report == null || report.isBlank()) {
            return "{\"status\":\"UNKNOWN\"}";
        }
        try {
            JsonNode root = objectMapper.readTree(report);
            removeSensitiveFields(root);
            String sanitized = objectMapper.writeValueAsString(root);
            return sanitized.length() <= MAX_REPORT_LENGTH
                    ? sanitized
                    : "{\"status\":\"TRUNCATED\"}";
        } catch (JsonProcessingException ex) {
            return "{\"status\":\"INVALID_REPORT\"}";
        }
    }

    private void removeSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitive(field.getKey())) {
                    fields.remove();
                } else {
                    removeSensitiveFields(field.getValue());
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                removeSensitiveFields(item);
            }
        }
    }

    private boolean isSensitive(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT);
        return normalized.contains("path")
                || normalized.contains("url")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey")
                || normalized.contains("api_key");
    }
}
