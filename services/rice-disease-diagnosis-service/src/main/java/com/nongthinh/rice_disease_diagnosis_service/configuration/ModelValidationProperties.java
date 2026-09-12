package com.nongthinh.rice_disease_diagnosis_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model-validation")
public record ModelValidationProperties(long maxArtifactBytes, int maxCachedModelVersions) {

    public ModelValidationProperties {
        if (maxArtifactBytes <= 0) {
            maxArtifactBytes = 104_857_600L;
        }
        if (maxCachedModelVersions <= 0) {
            maxCachedModelVersions = 4;
        }
    }
}
