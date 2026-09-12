package com.nongthinh.rice_disease_diagnosis_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceDetection;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelClassManifest;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;

class DetectionPostProcessorTest {

    private final DetectionPostProcessor postProcessor = new DetectionPostProcessor(
            new DiagnosisProperties(5, 5_000_000, 40_000_000, 0.25f, 0.45f, 4));

    @Test
    void parsesFeatureMajorOutputAppliesClassAwareNmsAndRestoresOriginalBoundingBoxes() {
        ActiveModelDeployment deployment = deployment();
        ImagePreprocessor.PreprocessedImage image = new ImagePreprocessor.PreprocessedImage(
                new float[200 * 200 * 3], 100, 50, 200, 200, 2f, 0, 50, false);
        float[][][] output = new float[][][] {{
                {100f, 100f, 190f},
                {100f, 100f, 100f},
                {80f, 80f, 50f},
                {80f, 80f, 50f},
                {0.90f, 0.80f, 0.10f},
                {0.10f, 0.10f, 0.80f}
        }};

        List<InferenceDetection> detections = postProcessor.parse(output, deployment, image);

        assertThat(detections).hasSize(2);
        assertThat(detections.get(0).classCode()).isEqualTo("RICE_BLAST");
        assertThat(detections.get(0).confidence()).isEqualTo(0.90f);
        assertThat(detections.get(0).boundingBox().x()).isEqualTo(30f);
        assertThat(detections.get(0).boundingBox().y()).isEqualTo(5f);
        assertThat(detections.get(0).boundingBox().width()).isEqualTo(40f);
        assertThat(detections.get(0).boundingBox().height()).isEqualTo(40f);
        assertThat(detections.get(1).classCode()).isEqualTo("HEALTHY");
    }

    @Test
    void supportsYoloOutputsThatIncludeAnObjectnessScore() {
        ImagePreprocessor.PreprocessedImage image = new ImagePreprocessor.PreprocessedImage(
                new float[200 * 200 * 3], 100, 100, 200, 200, 2f, 0, 0, false);
        float[][][] output = new float[][][] {{
                {100f}, {100f}, {80f}, {80f}, {0.9f}, {0.8f}, {0.2f}
        }};

        List<InferenceDetection> detections = postProcessor.parse(output, deployment(), image);

        assertThat(detections).singleElement().satisfies(detection -> {
            assertThat(detection.classCode()).isEqualTo("RICE_BLAST");
            assertThat(detection.confidence()).isCloseTo(0.72f, org.assertj.core.data.Offset.offset(0.0001f));
        });
    }

    private ActiveModelDeployment deployment() {
        return new ActiveModelDeployment(
                UUID.randomUUID(), UUID.randomUUID(), "RICE", "Rice detector", UUID.randomUUID(), "v1",
                UUID.randomUUID(), "a".repeat(64), 200, 200,
                List.of(
                        new ModelClassManifest(UUID.randomUUID(), 0, "RICE_BLAST", "Rice blast", "DISEASE"),
                        new ModelClassManifest(UUID.randomUUID(), 1, "HEALTHY", "Healthy", "HEALTHY")));
    }
}
