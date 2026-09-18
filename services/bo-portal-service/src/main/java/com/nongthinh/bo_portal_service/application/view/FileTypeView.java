package com.nongthinh.bo_portal_service.application.view;

import com.nongthinh.bo_portal_service.domain.fileconfig.FileType;

public record FileTypeView(String code, String contentType, String extension) {
    public static FileTypeView from(FileType type) {
        return new FileTypeView(type.code(), type.contentType(), type.extension());
    }
}
