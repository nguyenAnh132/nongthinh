package com.nongthinh.file_service.infra.client.boportal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.file_service.common.response.ApiResponse;
import com.nongthinh.file_service.infra.client.boportal.dto.FileUploadPolicyDto;

@FeignClient(name = "bo-portal-service", url = "${spring.security.api-key.clients.bo-portal-service.url}")
public interface BoPortalClient {
    @GetMapping("/internal/file-upload-policies/{purpose}")
    ApiResponse<FileUploadPolicyDto> getUploadPolicy(
            @PathVariable String purpose, @RequestHeader("X-API-KEY") String apiKey);
}
