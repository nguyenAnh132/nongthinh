package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.port.in.workflow.CorrelateDocumentsSubmittedUseCase;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorrelateDocumentsSubmittedUseCaseImpl implements CorrelateDocumentsSubmittedUseCase {

    private final RuntimeService runtimeService;

    @Override
    @Transactional
    public void execute(UUID brandProfileId) {
        String businessKey = "brand-" + brandProfileId;
        try {
            runtimeService.createMessageCorrelation(CamundaProcessConstants.DOCUMENTS_SUBMITTED_MESSAGE)
                    .processInstanceBusinessKey(businessKey)
                    .correlate();
            log.info("Correlated documents_submitted message for brandProfileId={}", brandProfileId);
        } catch (Exception ex) {
            log.error("Failed to correlate documents_submitted for brandProfileId={}", brandProfileId, ex);
            throw new BusinessException(ErrorCode.PROCESS_MESSAGE_CORRELATION_FAILED, ex);
        }
    }
}
