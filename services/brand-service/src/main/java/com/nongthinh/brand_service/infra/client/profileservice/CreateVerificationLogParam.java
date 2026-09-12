package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;

public record CreateVerificationLogParam(
        UUID adminUserId,
        String phoneCalled,
        String result,
        String note
) {
}
