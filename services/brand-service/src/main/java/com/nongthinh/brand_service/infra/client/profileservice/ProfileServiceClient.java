package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.brand_service.common.response.ApiResponse;

@FeignClient(name = "profile-service", url = "${spring.security.api-key.clients.profile-service.url}")
public interface ProfileServiceClient {

    @GetMapping("/internal/brand-profiles/{id}")
    ApiResponse<BrandProfileDto> getBrandProfileById(
            @PathVariable UUID id,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @GetMapping("/internal/brand-profiles/{id}/detail")
    ApiResponse<AdminBrandProfileDetailDto> getBrandProfileDetail(
            @PathVariable UUID id,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @PatchMapping("/internal/brand-profiles/{id}/status")
    ApiResponse<BrandProfileDto> updateBrandProfileStatus(
            @PathVariable UUID id,
            @RequestBody UpdateBrandStatusParam param,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @PostMapping("/internal/brand-profiles/{id}/documents-requested")
    ApiResponse<Void> recordDocumentsRequested(
            @PathVariable UUID id,
            @RequestBody RecordDocumentsRequestedParam param,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @PostMapping("/internal/brand-profiles/{id}/documents/review")
    ApiResponse<BrandProfileDto> reviewBrandDocument(
            @PathVariable UUID id,
            @RequestBody ReviewBrandDocumentParam param,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @PostMapping("/internal/brand-profiles/{id}/verifications")
    ApiResponse<Void> createVerificationLog(
            @PathVariable UUID id,
            @RequestBody CreateVerificationLogParam param,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
