package com.nongthinh.rice_disease_diagnosis_service.configuration;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.service.ModelRuntimeManager;

/**
 * Keeps the readiness probe from reporting ready merely because the JVM has
 * started. Activation warms the target version before the deployment becomes
 * active, so a ready replica has at least one serving runtime in its cache.
 */
@Component("diagnosisModelRuntime")
public class DiagnosisModelRuntimeHealthIndicator implements HealthIndicator {

    private final ModelRuntimeManager modelRuntimeManager;

    public DiagnosisModelRuntimeHealthIndicator(ModelRuntimeManager modelRuntimeManager) {
        this.modelRuntimeManager = modelRuntimeManager;
    }

    @Override
    public Health health() {
        int cachedVersionCount = modelRuntimeManager.cachedVersionCount();
        if (cachedVersionCount == 0) {
            return Health.outOfService()
                    .withDetail("cachedModelVersions", 0)
                    .withDetail("reason", "No warmed model runtime is available")
                    .build();
        }
        return Health.up().withDetail("cachedModelVersions", cachedVersionCount).build();
    }
}
