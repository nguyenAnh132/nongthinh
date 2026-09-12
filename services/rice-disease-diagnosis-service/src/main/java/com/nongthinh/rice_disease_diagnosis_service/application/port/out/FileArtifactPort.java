package com.nongthinh.rice_disease_diagnosis_service.application.port.out;

import java.util.UUID;

public interface FileArtifactPort {
    byte[] getArtifactContent(UUID artifactFileId);
}
