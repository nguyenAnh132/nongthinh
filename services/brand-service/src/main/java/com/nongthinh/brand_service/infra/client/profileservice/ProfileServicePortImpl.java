package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.common.response.ApiResponse;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProfileServicePortImpl implements ProfileServicePort {

    @Value("${spring.security.api-key.clients.profile-service.api-key}")
    private String profileServiceApiKey;

    private final ProfileServiceClient profileServiceClient;

    @Override
    public BrandProfileDto getBrandProfileById(UUID id) {
        ApiResponse<BrandProfileDto> response = profileServiceClient.getBrandProfileById(id, profileServiceApiKey);
        return requireResult(response, "Brand profile not found");
    }

    @Override
    public AdminBrandProfileDetailDto getBrandProfileDetail(UUID id) {
        ApiResponse<AdminBrandProfileDetailDto> response = profileServiceClient.getBrandProfileDetail(id,
                profileServiceApiKey);
        return requireResult(response, "Brand profile not found");
    }

    @Override
    public BrandProfileDto updateBrandProfileStatus(UUID id, String status, String rejectionReason, UUID actorUserId) {
        UpdateBrandStatusParam param = new UpdateBrandStatusParam(status, rejectionReason, actorUserId);
        ApiResponse<BrandProfileDto> response = profileServiceClient.updateBrandProfileStatus(id, param,
                profileServiceApiKey);
        return requireResult(response, "Brand profile not found");
    }

    @Override
    public void recordDocumentsRequested(UUID brandProfileId, UUID actorUserId) {
        profileServiceClient.recordDocumentsRequested(
                brandProfileId,
                new RecordDocumentsRequestedParam(actorUserId),
                profileServiceApiKey);
    }

    @Override
    public BrandProfileDto reviewBrandDocuments(
            UUID brandProfileId,
            UUID actorUserId,
            boolean documentsOk,
            String revisionReason) {
        ApiResponse<BrandProfileDto> response = profileServiceClient.reviewBrandDocument(
                brandProfileId,
                new ReviewBrandDocumentParam(actorUserId, documentsOk, revisionReason),
                profileServiceApiKey);
        return requireResult(response, "Brand profile not found");
    }

    @Override
    public void createVerificationLog(UUID brandProfileId, CreateVerificationLogParam param) {
        profileServiceClient.createVerificationLog(brandProfileId, param, profileServiceApiKey);
    }

    private <T> T requireResult(ApiResponse<T> response, String notFoundMessage) {
        if (response == null || response.getResult() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND, notFoundMessage);
        }
        return response.getResult();
    }
}
