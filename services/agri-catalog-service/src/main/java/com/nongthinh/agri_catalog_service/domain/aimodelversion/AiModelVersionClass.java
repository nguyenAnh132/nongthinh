package com.nongthinh.agri_catalog_service.domain.aimodelversion;

import java.util.Objects;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;

public final class AiModelVersionClass {

    private final UUID id;
    private final UUID modelVersionId;
    private final int classIndex;
    private final String classCode;
    private final String displayName;
    private final AiModelClassKind classKind;

    private AiModelVersionClass(
            UUID id,
            UUID modelVersionId,
            int classIndex,
            String classCode,
            String displayName,
            AiModelClassKind classKind) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.modelVersionId = Objects.requireNonNull(modelVersionId, "modelVersionId is required");
        if (classIndex < 0) {
            throw new IllegalArgumentException("classIndex must not be negative");
        }
        this.classIndex = classIndex;
        this.classCode = requireTrimmed(classCode, "classCode");
        this.displayName = requireTrimmed(displayName, "displayName");
        this.classKind = Objects.requireNonNull(classKind, "classKind is required");
    }

    public static AiModelVersionClass create(
            UUID id,
            UUID modelVersionId,
            int classIndex,
            String classCode,
            String displayName,
            AiModelClassKind classKind) {
        return new AiModelVersionClass(id, modelVersionId, classIndex, classCode, displayName, classKind);
    }

    public static AiModelVersionClass reconstruct(
            UUID id,
            UUID modelVersionId,
            int classIndex,
            String classCode,
            String displayName,
            AiModelClassKind classKind) {
        return new AiModelVersionClass(id, modelVersionId, classIndex, classCode, displayName, classKind);
    }

    private static String requireTrimmed(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public UUID getId() { return id; }
    public UUID getModelVersionId() { return modelVersionId; }
    public int getClassIndex() { return classIndex; }
    public String getClassCode() { return classCode; }
    public String getDisplayName() { return displayName; }
    public AiModelClassKind getClassKind() { return classKind; }
}
