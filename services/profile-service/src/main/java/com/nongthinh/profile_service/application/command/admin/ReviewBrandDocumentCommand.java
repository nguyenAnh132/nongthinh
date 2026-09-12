package com.nongthinh.profile_service.application.command.admin;

import java.util.UUID;

public record ReviewBrandDocumentCommand(
        UUID brandProfileId,
        UUID adminUserId,
        boolean documentsOk,
        String revisionReason
) {
}
