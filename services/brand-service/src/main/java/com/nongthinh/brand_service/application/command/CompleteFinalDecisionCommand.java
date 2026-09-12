package com.nongthinh.brand_service.application.command;

public record CompleteFinalDecisionCommand(
        String outcome,
        String rejectionReason
) {
}
