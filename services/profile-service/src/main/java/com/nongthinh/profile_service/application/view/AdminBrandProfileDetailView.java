package com.nongthinh.profile_service.application.view;

import java.util.List;

public record AdminBrandProfileDetailView(
        BrandProfileView profile,
        List<BrandDocumentView> documents,
        List<BrandVerificationLogView> verificationLogs,
        List<BrandLifecycleLogView> lifecycleLogs
) {
}
