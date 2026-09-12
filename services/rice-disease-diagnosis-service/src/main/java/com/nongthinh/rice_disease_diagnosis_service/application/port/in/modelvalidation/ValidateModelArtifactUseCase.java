package com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelvalidation;

import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelArtifactValidationCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelArtifactValidationResult;

public interface ValidateModelArtifactUseCase {
    ModelArtifactValidationResult execute(ModelArtifactValidationCommand command);
}
