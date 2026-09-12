package com.nongthinh.profile_service.presentation.dto.request.internal;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record InternalRecordDocumentsRequestedRequest(

    @NotNull(message = "ACTOR_USER_ID_REQUIRED")
    UUID actorUserId
) {
}
