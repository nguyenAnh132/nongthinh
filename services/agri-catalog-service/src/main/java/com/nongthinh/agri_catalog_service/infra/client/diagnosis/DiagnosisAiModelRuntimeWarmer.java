package com.nongthinh.agri_catalog_service.infra.client.diagnosis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.model.AiModelRuntimeWarmupRequest;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelRuntimeWarmer;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.infra.client.fileservice.FileServiceResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiagnosisAiModelRuntimeWarmer implements AiModelRuntimeWarmer {

    private final DiagnosisRuntimeWarmupClient diagnosisRuntimeWarmupClient;

    @Value("${spring.security.api-key.clients.rice-disease-diagnosis-service.api-key}")
    private String diagnosisServiceApiKey;

    @Override
    public void warm(AiModelRuntimeWarmupRequest request) {
        try {
            FileServiceResponse<DiagnosisRuntimeWarmupResponse> response = diagnosisRuntimeWarmupClient.warm(
                    request, diagnosisServiceApiKey);
            if (response == null || response.result() == null || !response.result().ready()) {
                throw new BusinessException(ErrorCode.MODEL_RUNTIME_NOT_READY);
            }
        } catch (FeignException ex) {
            throw new BusinessException(ErrorCode.MODEL_RUNTIME_NOT_READY, ex);
        }
    }
}
