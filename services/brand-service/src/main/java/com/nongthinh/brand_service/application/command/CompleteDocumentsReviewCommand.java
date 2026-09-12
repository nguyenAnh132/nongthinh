package com.nongthinh.brand_service.application.command;

public record CompleteDocumentsReviewCommand(
        boolean documentsOk,
        String revisionReason
) {
}
