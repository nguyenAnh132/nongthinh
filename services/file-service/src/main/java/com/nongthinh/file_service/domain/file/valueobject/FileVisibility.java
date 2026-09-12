package com.nongthinh.file_service.domain.file.valueobject;

public enum FileVisibility {
    PUBLIC,
    PRIVATE;

    public static FileVisibility forPurpose(FilePurpose purpose) {
        return switch (purpose) {
            case DIAGNOSIS_IMAGE, MODEL_ARTIFACT -> PRIVATE;
            default -> PUBLIC;
        };
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
