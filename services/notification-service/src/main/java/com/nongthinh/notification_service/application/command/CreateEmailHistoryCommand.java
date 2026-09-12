package com.nongthinh.notification_service.application.command;

import java.time.Instant;

public record CreateEmailHistoryCommand(
    String userId,
    Long templateId,
    Long purposeId,
    String status,
    Instant sendAt
) {

}
