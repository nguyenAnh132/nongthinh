package com.nongthinh.agri_catalog_service.infra.client.diagnosis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.agri_catalog_service.application.model.AiModelRuntimeWarmupRequest;
import com.nongthinh.agri_catalog_service.infra.client.fileservice.FileServiceResponse;

@FeignClient(
        name = "rice-disease-diagnosis-runtime-service",
        url = "${spring.security.api-key.clients.rice-disease-diagnosis-service.url}"
)
public interface DiagnosisRuntimeWarmupClient {

    @PostMapping("/internal/model-runtimes/warm")
    FileServiceResponse<DiagnosisRuntimeWarmupResponse> warm(
            @RequestBody AiModelRuntimeWarmupRequest request,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
