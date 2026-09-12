package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis;

import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryDetailView;

public interface GetDiagnosisHistoryUseCase {
    DiagnosisHistoryDetailView execute(UUID diagnosisId);
}
