package com.nongthinh.rice_disease_diagnosis_service.application.model;

import java.util.List;

public record InferenceImageResult(
        int width,
        int height,
        List<InferenceDetection> detections
) {
}
