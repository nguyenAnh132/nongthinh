package com.nongthinh.auth_service.application.port.out.otp;

import java.time.Instant;

public record OtpRecord(
        String otpHash,
        int attemptCount,
        int resendCount,
        Instant lastSentAt
) {
    public static OtpRecord initial(String otpHash, Instant lastSentAt) {
        return new OtpRecord(otpHash, 0, 0, lastSentAt);
    }

    public OtpRecord withIncrementedAttempt() {
        return new OtpRecord(otpHash, attemptCount + 1, resendCount, lastSentAt);
    }

    public OtpRecord nextResend(String newOtpHash, Instant lastSentAt) {
        return new OtpRecord(newOtpHash, 0, resendCount + 1, lastSentAt);
    }
}
