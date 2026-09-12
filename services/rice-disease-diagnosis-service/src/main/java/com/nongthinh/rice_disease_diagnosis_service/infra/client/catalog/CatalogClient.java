package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "agri-catalog-service", url = "${spring.security.api-key.clients.agri-catalog-service.url}")
public interface CatalogClient {

    @GetMapping("/internal/ai-model-deployments/resolve")
    CatalogResponse<CatalogDeploymentDto> resolveDeployment(
            @RequestParam UUID cropTypeId,
            @RequestHeader("X-API-KEY") String apiKey);

    @PostMapping("/internal/ai-model-mappings/resolve")
    CatalogResponse<List<CatalogDiseaseMappingDto>> resolveMappings(
            @RequestBody CatalogMappingResolutionRequest request,
            @RequestHeader("X-API-KEY") String apiKey);
}
