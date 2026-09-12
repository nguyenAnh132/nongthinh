package com.nongthinh.profile_service.presentation.dto.request.internal;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InternalReviewBrandDocumentRequest(

    @NotNull(message = "ACTOR_USER_ID_REQUIRED")
    UUID adminUserId,

    boolean documentsOk,

    @Size(max = 1000, message = "REVISION_REASON_TOO_LONG")
    String revisionReason
) {
}
