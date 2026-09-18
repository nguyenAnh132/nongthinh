package com.nongthinh.file_service.infra.caching.upload;

import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public final class FileUploadPolicyCacheKeys {
    private FileUploadPolicyCacheKeys() {
    }

    public static String policy(FilePurpose purpose) {
        return "file-service:upload-policy:v1:" + purpose.name();
    }
}
