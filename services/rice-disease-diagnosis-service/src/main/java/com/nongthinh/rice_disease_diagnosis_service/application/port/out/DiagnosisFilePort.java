package com.nongthinh.rice_disease_diagnosis_service.application.port.out;

import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.application.model.DiagnosisFileMetadata;

public interface DiagnosisFilePort {
    DiagnosisFileMetadata getFileMetadata(UUID fileId);

    byte[] getFileContent(UUID fileId);

    /**
     * Removes a scan image after no diagnosis history references it anymore. The file-service
     * endpoint is idempotent so callers can safely retry a failed cleanup task.
     */
    void deleteDiagnosisImage(UUID fileId);
}
