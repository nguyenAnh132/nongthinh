package com.nongthinh.rice_disease_diagnosis_service.configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.service.ModelRuntimeManager;

@Component
public class DiagnosisRuntimeMetrics {

    public DiagnosisRuntimeMetrics(MeterRegistry meterRegistry, ModelRuntimeManager modelRuntimeManager) {
        Gauge.builder("diagnosis.runtime.cached_versions", modelRuntimeManager, ModelRuntimeManager::cachedVersionCount)
                .description("Number of ONNX model versions retained in the local runtime cache")
                .register(meterRegistry);
    }
}
