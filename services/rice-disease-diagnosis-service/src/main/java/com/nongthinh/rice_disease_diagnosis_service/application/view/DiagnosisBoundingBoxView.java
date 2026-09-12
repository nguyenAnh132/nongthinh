package com.nongthinh.rice_disease_diagnosis_service.application.view;

import com.nongthinh.rice_disease_diagnosis_service.application.model.BoundingBox;

public record DiagnosisBoundingBoxView(float x, float y, float width, float height) {
    public static DiagnosisBoundingBoxView from(BoundingBox box) {
        return new DiagnosisBoundingBoxView(box.x(), box.y(), box.width(), box.height());
    }
}
