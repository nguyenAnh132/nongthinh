package com.nongthinh.auth_service.infra.persistence.otp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_otps")
@Getter
@Setter
@NoArgsConstructor
public class JpaOtpEntity {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "resend_count", nullable = false)
    private int resendCount;
    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "consumed_at")
    private Instant consumedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
