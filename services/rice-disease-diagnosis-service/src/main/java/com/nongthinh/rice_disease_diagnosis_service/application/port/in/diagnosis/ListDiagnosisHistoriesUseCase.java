package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis;

import java.util.List;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryListItemView;

public interface ListDiagnosisHistoriesUseCase {
    List<DiagnosisHistoryListItemView> execute();
}
