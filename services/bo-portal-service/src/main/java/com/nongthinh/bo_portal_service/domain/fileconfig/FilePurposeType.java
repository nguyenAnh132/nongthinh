package com.nongthinh.bo_portal_service.domain.fileconfig;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class FilePurposeType {
    private final UUID id;
    private final FilePurpose purpose;
    private final FileType fileType;
    private boolean enabled;
    private final Instant createdAt;
    private Instant updatedAt;

    private FilePurposeType(UUID id, FilePurpose purpose, FileType fileType, boolean enabled,
            Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.purpose = Objects.requireNonNull(purpose);
        this.fileType = Objects.requireNonNull(fileType);
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static FilePurposeType reconstruct(UUID id, FilePurpose purpose, FileType fileType,
            boolean enabled, Instant createdAt, Instant updatedAt) {
        return new FilePurposeType(id, purpose, fileType, enabled, createdAt, updatedAt);
    }

    public void changeEnabled(boolean enabled, Instant now) {
        this.enabled = enabled;
        this.updatedAt = Objects.requireNonNull(now);
    }
}
