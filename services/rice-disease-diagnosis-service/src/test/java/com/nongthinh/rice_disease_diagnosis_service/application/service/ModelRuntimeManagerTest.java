package com.nongthinh.rice_disease_diagnosis_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModelRuntimeManagerTest {

    @Test
    void resolvesDynamicNchwInputToConfiguredImageSizeForWarmup() throws Exception {
        assertThat(ModelRuntimeManager.resolvedInputShape(
                new long[] {-1, 3, -1, -1}, 640, 480))
                .containsExactly(1, 3, 480, 640);
    }

    @Test
    void resolvesDynamicNhwcInputToConfiguredImageSizeForWarmup() throws Exception {
        assertThat(ModelRuntimeManager.resolvedInputShape(
                new long[] {-1, -1, -1, 3}, 640, 480))
                .containsExactly(1, 480, 640, 3);
    }
}
