package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.GetDiagnosisHistoryUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryDetailView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisResponseView;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDiagnosisHistoryUseCaseImpl implements GetDiagnosisHistoryUseCase {
    private final CurrentUserProvider currentUserProvider;
    private final DiagnosisHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public DiagnosisHistoryDetailView execute(UUID diagnosisId) {
        var history = historyRepository.findByIdAndFarmerUserId(
                        diagnosisId, currentUserProvider.getCurrentUser().userId())
                .orElseThrow(() -> new DiagnosisException(ErrorCode.DIAGNOSIS_HISTORY_NOT_FOUND));
        try {
            DiagnosisResponseView snapshot = objectMapper.readValue(
                    history.getResultSnapshot(), DiagnosisResponseView.class);
            return DiagnosisHistoryDetailView.from(history, snapshot);
        } catch (JsonProcessingException ex) {
            throw new DiagnosisException(ErrorCode.INTERNAL_ERROR, ex);
        }
    }
}
