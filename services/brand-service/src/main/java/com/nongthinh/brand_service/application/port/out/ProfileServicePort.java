package com.nongthinh.brand_service.application.port.out;

import java.util.UUID;
import com.nongthinh.brand_service.infra.client.profileservice.AdminBrandProfileDetailDto;
import com.nongthinh.brand_service.infra.client.profileservice.BrandProfileDto;
import com.nongthinh.brand_service.infra.client.profileservice.CreateVerificationLogParam;

public interface ProfileServicePort {

    BrandProfileDto getBrandProfileById(UUID id);

    AdminBrandProfileDetailDto getBrandProfileDetail(UUID id);

    BrandProfileDto updateBrandProfileStatus(UUID id, String status, String rejectionReason, UUID actorUserId);

    void recordDocumentsRequested(UUID brandProfileId, UUID actorUserId);

    BrandProfileDto reviewBrandDocuments(
            UUID brandProfileId,
            UUID actorUserId,
            boolean documentsOk,
            String revisionReason
    );

    void createVerificationLog(UUID brandProfileId, CreateVerificationLogParam param);
}
