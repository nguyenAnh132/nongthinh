package com.nongthinh.brand_service.infra.client.profileservice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminBrandProfileDetailDto(
        BrandProfileDto profile,
        List<BrandDocumentDto> documents,
        List<BrandVerificationLogDto> verificationLogs,
        List<BrandLifecycleLogDto> lifecycleLogs
) {
}
