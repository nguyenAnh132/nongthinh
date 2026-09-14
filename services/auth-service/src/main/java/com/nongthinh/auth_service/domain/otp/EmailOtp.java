package com.nongthinh.auth_service.domain.otp;

import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EmailOtp {

    private final UUID id;
    private final UUID userId;
    private final Email email;
    private String otpHash;
    private int attemptCount;
    private int resendCount;
    private Instant lastSentAt;
    private Instant expiresAt;
    private Instant consumedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private EmailOtp(UUID id, UUID userId, Email email, String otpHash, int attemptCount,
            int resendCount, Instant lastSentAt, Instant expiresAt, Instant consumedAt,
            Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.email = Objects.requireNonNull(email, "email is required");
        validateChallenge(otpHash, lastSentAt, expiresAt);
        if (attemptCount < 0 || resendCount < 0) {
            throw new IllegalArgumentException("OTP counters must not be negative");
        }
        if (consumedAt != null && consumedAt.isBefore(lastSentAt)) {
            throw new IllegalArgumentException("consumedAt must not precede lastSentAt");
        }
        this.otpHash = otpHash;
        this.attemptCount = attemptCount;
        this.resendCount = resendCount;
        this.lastSentAt = lastSentAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static EmailOtp issue(UUID id, UUID userId, Email email, String otpHash,
            Instant now, Instant expiresAt) {
        return new EmailOtp(id, userId, email, otpHash, 0, 0, now, expiresAt, null, now, now);
    }

    public static EmailOtp reconstruct(UUID id, UUID userId, Email email, String otpHash,
            int attemptCount, int resendCount, Instant lastSentAt, Instant expiresAt,
            Instant consumedAt, Instant createdAt, Instant updatedAt) {
        return new EmailOtp(id, userId, email, otpHash, attemptCount, resendCount,
                lastSentAt, expiresAt, consumedAt, createdAt, updatedAt);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public void ensureCanVerify(Instant now, int maxAttempts) {
        if (consumedAt != null || isExpired(now)) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        if (attemptCount >= maxAttempts) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED);
        }
    }

    public void recordFailedAttempt(Instant now, int maxAttempts) {
        ensureCanVerify(now, maxAttempts);
        attemptCount++;
        updatedAt = now;
    }

    public void consume(Instant now, int maxAttempts) {
        ensureCanVerify(now, maxAttempts);
        consumedAt = now;
        updatedAt = now;
    }

    public void ensureCanResend(Instant now, int maxResend, int cooldownSeconds) {
        if (consumedAt != null) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        // Expiration starts a new challenge window, matching the former Redis TTL behavior.
        if (isExpired(now)) {
            return;
        }
        if (resendCount >= maxResend) {
            throw new BusinessException(ErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
        }
        if (now.isBefore(lastSentAt.plusSeconds(cooldownSeconds))) {
            throw new BusinessException(ErrorCode.OTP_RESEND_TOO_FREQUENT);
        }
    }

    public void resend(String newHash, Instant now, Instant newExpiresAt,
            int maxResend, int cooldownSeconds) {
        ensureCanResend(now, maxResend, cooldownSeconds);
        validateChallenge(newHash, now, newExpiresAt);
        resendCount = isExpired(now) ? 0 : resendCount + 1;
        attemptCount = 0;
        otpHash = newHash;
        lastSentAt = now;
        expiresAt = newExpiresAt;
        updatedAt = now;
    }

    private static void validateChallenge(String hash, Instant sentAt, Instant expiresAt) {
        if (hash == null || hash.isBlank() || hash.length() > 255) {
            throw new IllegalArgumentException("OTP hash is invalid");
        }
        Objects.requireNonNull(sentAt, "lastSentAt is required");
        Objects.requireNonNull(expiresAt, "expiresAt is required");
        if (!expiresAt.isAfter(sentAt)) {
            throw new IllegalArgumentException("expiresAt must be after lastSentAt");
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Email getEmail() { return email; }
    public String getOtpHash() { return otpHash; }
    public int getAttemptCount() { return attemptCount; }
    public int getResendCount() { return resendCount; }
    public Instant getLastSentAt() { return lastSentAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
