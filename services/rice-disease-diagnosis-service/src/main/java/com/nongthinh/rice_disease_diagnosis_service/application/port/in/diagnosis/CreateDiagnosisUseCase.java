package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis;

import com.nongthinh.rice_disease_diagnosis_service.application.command.DiagnosisCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisResponseView;

public interface CreateDiagnosisUseCase {
    DiagnosisResponseView execute(DiagnosisCommand command);
}
