package com.nongthinh.agri_catalog_service.infra.client.diagnosis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationRequest;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationResult;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionValidator;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.infra.client.fileservice.FileServiceResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiagnosisAiModelVersionValidator implements AiModelVersionValidator {

    private final DiagnosisValidationClient diagnosisValidationClient;

    @Value("${spring.security.api-key.clients.rice-disease-diagnosis-service.api-key}")
    private String diagnosisServiceApiKey;

    @Override
    public AiModelValidationResult validate(AiModelValidationRequest request) {
        try {
            FileServiceResponse<DiagnosisValidationResponse> response = diagnosisValidationClient.validate(
                    request, diagnosisServiceApiKey);
            if (response == null || response.result() == null) {
                log.warn(
                        "Diagnosis model validation returned an empty response: modelVersionId={}",
                        request.modelVersionId());
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_UNAVAILABLE);
            }
            DiagnosisValidationResponse result = response.result();
            return new AiModelValidationResult(result.valid(), result.report());
        } catch (FeignException ex) {
            log.warn(
                    "Diagnosis model validation HTTP call failed: modelVersionId={}, status={}, exceptionType={}",
                    request.modelVersionId(), ex.status(), ex.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_UNAVAILABLE, ex);
        }
    }
}
