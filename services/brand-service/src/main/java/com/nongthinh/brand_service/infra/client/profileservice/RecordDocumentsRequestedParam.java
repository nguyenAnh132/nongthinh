package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;

public record RecordDocumentsRequestedParam(
    UUID actorUserId
) {
}
