package com.nongthinh.brand_service.application.command;

public record CompletePhoneVerificationCommand(
        String phoneCalled,
        String result,
        String note
) {
}
