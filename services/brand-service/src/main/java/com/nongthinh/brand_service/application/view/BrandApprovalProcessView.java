package com.nongthinh.brand_service.application.view;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BrandApprovalProcessView(
        UUID id,
        UUID brandProfileId,
        String camundaProcessInstanceId,
        String camundaBusinessKey,
        String status,
        UUID assignedReviewerId,
        Instant startedAt,
        Instant completedAt,
        List<String> activeTaskDefinitionKeys
) {
}
