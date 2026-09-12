package com.nongthinh.brand_service.application.view;

import java.time.Instant;
import java.util.Map;

public record TicketView(
        String taskId,
        String taskDefinitionKey,
        String taskName,
        String processInstanceId,
        String businessKey,
        String assignee,
        Instant createdAt,
        Map<String, Object> variables
) {
}
