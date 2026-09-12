package com.nongthinh.agri_catalog_service.infra.client.diagnosis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationRequest;
import com.nongthinh.agri_catalog_service.infra.client.fileservice.FileServiceResponse;

@FeignClient(
        name = "rice-disease-diagnosis-service",
        url = "${spring.security.api-key.clients.rice-disease-diagnosis-service.url}"
)
public interface DiagnosisValidationClient {

    @PostMapping("/internal/model-validations")
    FileServiceResponse<DiagnosisValidationResponse> validate(
            @RequestBody AiModelValidationRequest request,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
