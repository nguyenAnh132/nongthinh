package com.nongthinh.auth_service.infra.client.profileservice.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminProfileDto(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
