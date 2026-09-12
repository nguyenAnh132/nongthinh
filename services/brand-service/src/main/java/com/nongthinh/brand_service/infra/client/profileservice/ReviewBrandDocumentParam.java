package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;

public record ReviewBrandDocumentParam(
    UUID adminUserId,
    boolean documentsOk,
    String revisionReason
) {
}
