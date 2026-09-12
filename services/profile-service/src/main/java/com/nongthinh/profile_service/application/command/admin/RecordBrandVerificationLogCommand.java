package com.nongthinh.profile_service.application.command.admin;

import java.util.UUID;

public record RecordBrandVerificationLogCommand(
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        String result,
        String note
) {
}
