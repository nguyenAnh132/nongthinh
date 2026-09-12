package com.nongthinh.brand_service.application.command;

import java.util.UUID;

public record StartBrandApprovalProcessCommand(
        UUID brandProfileId,
        UUID userId,
        String brandName
) {
}
