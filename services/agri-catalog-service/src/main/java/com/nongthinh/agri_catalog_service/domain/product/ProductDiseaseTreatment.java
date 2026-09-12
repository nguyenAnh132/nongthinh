package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;

public class ProductDiseaseTreatment {

    private final UUID id;
    private final UUID productId;
    private final UUID diseaseId;
    private final UUID brandId;

    private EffectivenessLevel effectivenessLevel;
    private int priority;

    private String dosage;
    private String applicationMethod;
    private String applicationTiming;
    private String frequencyInstruction;
    private String treatmentNote;

    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private ProductDiseaseTreatment(
        UUID id,
        UUID productId,
        UUID diseaseId,
        UUID brandId,
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        this.id = id;
        this.productId = productId;
        this.diseaseId = diseaseId;
        this.brandId = brandId;
        this.effectivenessLevel = effectivenessLevel;
        this.priority = priority;
        this.dosage = dosage;
        this.applicationMethod = applicationMethod;
        this.applicationTiming = applicationTiming;
        this.frequencyInstruction = frequencyInstruction;
        this.treatmentNote = treatmentNote;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static ProductDiseaseTreatment create(
        UUID id,
        UUID productId,
        UUID diseaseId,
        UUID brandId,
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote,
        UUID createdBy,
        Instant now
    ) {
        validatePriority(priority);
        return new ProductDiseaseTreatment(
            id,
            productId,
            diseaseId,
            brandId,
            effectivenessLevel,
            priority,
            dosage,
            applicationMethod,
            applicationTiming,
            frequencyInstruction,
            treatmentNote,
            now,
            createdBy,
            now,
            createdBy,
            null,
            null
        );
    }

    public static ProductDiseaseTreatment reconstruct(
        UUID id,
        UUID productId,
        UUID diseaseId,
        UUID brandId,
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        return new ProductDiseaseTreatment(
            id,
            productId,
            diseaseId,
            brandId,
            effectivenessLevel,
            priority,
            dosage,
            applicationMethod,
            applicationTiming,
            frequencyInstruction,
            treatmentNote,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy,
            deletedAt,
            deletedBy
        );
    }

    public void update(
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote,
        UUID updatedBy,
        Instant now
    ) {
        validatePriority(priority);
        this.effectivenessLevel = effectivenessLevel;
        this.priority = priority;
        this.dosage = dosage;
        this.applicationMethod = applicationMethod;
        this.applicationTiming = applicationTiming;
        this.frequencyInstruction = frequencyInstruction;
        this.treatmentNote = treatmentNote;

        this.touch(now, updatedBy);
    }

    public void changeEffectivenessLevel(
        EffectivenessLevel effectivenessLevel,
        UUID updatedBy,
        Instant now
    ) {
        this.effectivenessLevel = effectivenessLevel;
        this.touch(now, updatedBy);
    }

    public void changePriority(
        int priority,
        UUID updatedBy,
        Instant now
    ) {
        validatePriority(priority);
        this.priority = priority;
        this.touch(now, updatedBy);
    }

    public void changeDosage(
        String dosage,
        UUID updatedBy,
        Instant now
    ) {
        this.dosage = dosage;
        this.touch(now, updatedBy);
    }

    public void changeApplicationMethod(
        String applicationMethod,
        UUID updatedBy,
        Instant now
    ) {
        this.applicationMethod = applicationMethod;
        this.touch(now, updatedBy);
    }

    public void changeApplicationTiming(
        String applicationTiming,
        UUID updatedBy,
        Instant now
    ) {
        this.applicationTiming = applicationTiming;
        this.touch(now, updatedBy);
    }

    public void changeFrequencyInstruction(
        String frequencyInstruction,
        UUID updatedBy,
        Instant now
    ) {
        this.frequencyInstruction = frequencyInstruction;
        this.touch(now, updatedBy);
    }

    public void changeTreatmentNote(
        String treatmentNote,
        UUID updatedBy,
        Instant now
    ) {
        this.treatmentNote = treatmentNote;
        this.touch(now, updatedBy);
    }

    public void delete(
        UUID deletedBy,
        Instant now
    ) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;

        this.touch(now, deletedBy);
    }

    public void restore(
        UUID restoredBy,
        Instant now
    ) {
        this.deletedAt = null;
        this.deletedBy = null;

        this.touch(now, restoredBy);
    }

    private void touch(
        Instant now,
        UUID updatedBy
    ) {
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    private static void validatePriority(int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must not be negative");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getDiseaseId() {
        return diseaseId;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public EffectivenessLevel getEffectivenessLevel() {
        return effectivenessLevel;
    }

    public int getPriority() {
        return priority;
    }

    public String getDosage() {
        return dosage;
    }

    public String getApplicationMethod() {
        return applicationMethod;
    }

    public String getApplicationTiming() {
        return applicationTiming;
    }

    public String getFrequencyInstruction() {
        return frequencyInstruction;
    }

    public String getTreatmentNote() {
        return treatmentNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
