package com.nongthinh.auth_service.infra.client.profileservice;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.infra.client.profileservice.dto.AdminProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.BrandProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.FarmerProfileDto;

@FeignClient(name = "profile-service", url = "${spring.security.api-key.clients.profile-service.url}")
public interface ProfileClient {

    @org.springframework.web.bind.annotation.PostMapping("/internal/registration-profiles")
    void completeRegistration(@org.springframework.web.bind.annotation.RequestBody
            com.nongthinh.auth_service.infra.client.profileservice.dto.CompleteProfileRegistrationParam request,
            @RequestHeader("X-API-KEY") String apiKey);

    @GetMapping("/internal/farmer-profiles/users/{userId}")
    ApiResponse<FarmerProfileDto> getFarmerProfile(
            @PathVariable UUID userId,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @GetMapping("/internal/brand-profiles/users/{userId}")
    ApiResponse<BrandProfileDto> getBrandProfile(
            @PathVariable UUID userId,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @GetMapping("/internal/admin-profiles/users/{userId}")
    ApiResponse<AdminProfileDto> getAdminProfile(
            @PathVariable UUID userId,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
