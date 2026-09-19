package com.nongthinh.auth_service.infra.client.profileservice.dto;

import java.util.UUID;

public record CompleteProfileRegistrationParam(UUID userId, String role, String firstName, String lastName,
        String gender, String phone, String brandName, String representativeName, String representativePhone,
        String representativeEmail) {
}
