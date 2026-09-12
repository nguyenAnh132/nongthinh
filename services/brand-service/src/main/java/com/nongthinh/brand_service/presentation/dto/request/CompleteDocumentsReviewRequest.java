package com.nongthinh.brand_service.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record CompleteDocumentsReviewRequest(
        boolean documentsOk,

        @Size(max = 1000, message = "REVISION_REASON_TOO_LONG")
        String revisionReason
) {
}
