package com.nongthinh.file_service.infra.client.boportal.dto;

import java.util.Set;

public record FileUploadPolicyDto(String purpose, Long maxSizeBytes, Set<String> allowedContentTypes) {
}
