package com.nongthinh.auth_service.infra.client.profileservice;

import java.util.Optional;
import java.util.UUID;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.infra.client.profileservice.dto.AdminProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.BrandProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.FarmerProfileDto;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfileQueryImpl implements ProfileQuery {

    private final ProfileClient profileClient;
    private final ProfileViewMapper profileViewMapper;
    private final String apiKey;

    public ProfileQueryImpl(
            ProfileClient profileClient,
            ProfileViewMapper profileViewMapper,
            @Value("${spring.security.api-key.clients.profile-service.api-key}") String apiKey) {
        this.profileClient = profileClient;
        this.profileViewMapper = profileViewMapper;
        this.apiKey = apiKey;
    }

    @Override
    public Optional<ProfileView> getFarmerProfile(UUID userId) {
        try {
            ApiResponse<FarmerProfileDto> response = profileClient.getFarmerProfile(userId, apiKey);
            return Optional.of(profileViewMapper.fromFarmer(response.getResult()));
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.PROFILE_SERVICE_GET_FARMER_PROFILE_FAILED);
        }
    }

    @Override
    public Optional<ProfileView> getBrandProfile(UUID userId) {
        try {
            ApiResponse<BrandProfileDto> response = profileClient.getBrandProfile(userId, apiKey);
            return Optional.of(profileViewMapper.fromBrand(response.getResult()));
        } catch (FeignException ex) {
            throw ex;
        }
    }

    @Override
    public Optional<ProfileView> getAdminProfile(UUID userId) {
        try {
            ApiResponse<AdminProfileDto> response = profileClient.getAdminProfile(userId, apiKey);
            return Optional.of(profileViewMapper.fromAdmin(response.getResult()));
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.PROFILE_SERVICE_GET_ADMIN_PROFILE_FAILED);
        }
    }

}
