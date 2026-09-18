package com.nongthinh.bo_portal_service.application.view;

import java.time.Instant;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurposeType;

public record FilePurposeTypeView(String code, String contentType, String extension, boolean enabled, Instant updatedAt) {
    public static FilePurposeTypeView from(FilePurposeType mapping) {
        var type = mapping.getFileType();
        return new FilePurposeTypeView(type.code(), type.contentType(), type.extension(),
                mapping.isEnabled(), mapping.getUpdatedAt());
    }
}
