package com.nongthinh.auth_service.infra.client.profileservice.dto;

import java.time.Instant;
import java.util.UUID;

public record FarmerProfileDto(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String gender,
        String phone,
        String provinceId,
        UUID communeId,
        String addressDetail,
        String avatarUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
