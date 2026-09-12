package com.nongthinh.rice_disease_diagnosis_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "diagnosis")
public record DiagnosisProperties(
        int maxFilesPerRequest,
        long maxImageBytes,
        long maxImagePixels,
        float confidenceThreshold,
        float nmsIouThreshold,
        int maxConcurrentInferences
) {
    public DiagnosisProperties {
        if (maxFilesPerRequest <= 0) maxFilesPerRequest = 5;
        if (maxImageBytes <= 0) maxImageBytes = 5L * 1024 * 1024;
        if (maxImagePixels <= 0) maxImagePixels = 40_000_000L;
        if (confidenceThreshold <= 0 || confidenceThreshold > 1) confidenceThreshold = 0.25f;
        if (nmsIouThreshold <= 0 || nmsIouThreshold > 1) nmsIouThreshold = 0.45f;
        if (maxConcurrentInferences <= 0) maxConcurrentInferences = 4;
    }
}
