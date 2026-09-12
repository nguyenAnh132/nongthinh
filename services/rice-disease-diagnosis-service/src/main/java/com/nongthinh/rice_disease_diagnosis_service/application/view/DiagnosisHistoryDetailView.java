package com.nongthinh.rice_disease_diagnosis_service.application.view;

import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;

public record DiagnosisHistoryDetailView(
        DiagnosisHistoryListItemView history,
        DiagnosisResponseView snapshot
) {
    public static DiagnosisHistoryDetailView from(DiagnosisHistory history, DiagnosisResponseView snapshot) {
        return new DiagnosisHistoryDetailView(DiagnosisHistoryListItemView.from(history), snapshot);
    }
}
