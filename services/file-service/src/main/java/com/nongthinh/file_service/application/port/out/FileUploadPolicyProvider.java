package com.nongthinh.file_service.application.port.out;

import com.nongthinh.file_service.domain.file.FileUploadPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public interface FileUploadPolicyProvider {
    FileUploadPolicy getPolicy(FilePurpose purpose);
}
