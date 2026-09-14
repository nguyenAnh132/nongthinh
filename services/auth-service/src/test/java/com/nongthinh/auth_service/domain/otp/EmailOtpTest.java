package com.nongthinh.auth_service.domain.otp;

import static org.assertj.core.api.Assertions.*;

import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmailOtpTest {
    private static final Instant NOW = Instant.parse("2026-09-14T00:00:00Z");

    private EmailOtp issue() {
        return EmailOtp.issue(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                Email.of("otp@example.com"), "hash", NOW, NOW.plusSeconds(300));
    }

    @Test
    void expiresAtExactBoundary() {
        EmailOtp otp = issue();
        otp.ensureCanVerify(NOW.plusSeconds(299), 3);
        assertError(() -> otp.ensureCanVerify(NOW.plusSeconds(300), 3), ErrorCode.OTP_EXPIRED);
    }

    @Test
    void failedAttemptsDoNotExtendExpiryAndEnforceLimit() {
        EmailOtp otp = issue();
        for (int i = 0; i < 3; i++) {
            otp.recordFailedAttempt(NOW.plusSeconds(i), 3);
        }
        assertThat(otp.getExpiresAt()).isEqualTo(NOW.plusSeconds(300));
        assertError(() -> otp.consume(NOW.plusSeconds(4), 3), ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED);
    }

    @Test
    void consumedOtpCannotBeVerifiedOrResent() {
        EmailOtp otp = issue();
        otp.consume(NOW.plusSeconds(1), 3);
        assertError(() -> otp.ensureCanVerify(NOW.plusSeconds(2), 3), ErrorCode.OTP_EXPIRED);
        assertError(() -> otp.ensureCanResend(NOW.plusSeconds(400), 3, 60), ErrorCode.OTP_EXPIRED);
    }

    @Test
    void resendEnforcesCooldownAndLimitThenRestartsExpiredWindow() {
        EmailOtp otp = issue();
        assertError(() -> otp.ensureCanResend(NOW.plusSeconds(59), 1, 60), ErrorCode.OTP_RESEND_TOO_FREQUENT);
        otp.recordFailedAttempt(NOW, 3);
        otp.resend("new-hash", NOW.plusSeconds(60), NOW.plusSeconds(360), 1, 60);
        assertThat(otp.getAttemptCount()).isZero();
        assertThat(otp.getResendCount()).isEqualTo(1);
        assertError(() -> otp.ensureCanResend(NOW.plusSeconds(120), 1, 60), ErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
        otp.resend("next-hash", NOW.plusSeconds(360), NOW.plusSeconds(660), 1, 60);
        assertThat(otp.getResendCount()).isZero();
        assertThat(otp.getOtpHash()).isEqualTo("next-hash");
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(errorCode));
    }
}
