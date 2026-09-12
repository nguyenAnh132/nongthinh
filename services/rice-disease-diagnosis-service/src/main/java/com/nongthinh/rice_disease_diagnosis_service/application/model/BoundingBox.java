package com.nongthinh.rice_disease_diagnosis_service.application.model;

public record BoundingBox(float x, float y, float width, float height) {
    public float area() { return Math.max(0, width) * Math.max(0, height); }
}
