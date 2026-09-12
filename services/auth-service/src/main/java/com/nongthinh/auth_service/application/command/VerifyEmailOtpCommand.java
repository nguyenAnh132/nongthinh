package com.nongthinh.auth_service.application.command;

public record VerifyEmailOtpCommand(String email, String otp) {
}
