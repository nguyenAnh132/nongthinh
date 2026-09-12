package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.ListDiagnosisHistoriesUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryListItemView;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListDiagnosisHistoriesUseCaseImpl implements ListDiagnosisHistoriesUseCase {
    private final CurrentUserProvider currentUserProvider;
    private final DiagnosisHistoryRepository historyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisHistoryListItemView> execute() {
        return historyRepository.findLatestByFarmerUserId(currentUserProvider.getCurrentUser().userId()).stream()
                .map(DiagnosisHistoryListItemView::from)
                .toList();
    }
}
