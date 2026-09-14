package com.nongthinh.auth_service.infra.persistence.otp;

import com.nongthinh.auth_service.domain.otp.EmailOtp;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import org.springframework.stereotype.Component;

@Component
public class OtpPersistenceMapper {
    public EmailOtp toDomain(JpaOtpEntity entity) {
        return EmailOtp.reconstruct(entity.getId(), entity.getUserId(), Email.of(entity.getEmail()),
                entity.getOtpHash(), entity.getAttemptCount(), entity.getResendCount(),
                entity.getLastSentAt(), entity.getExpiresAt(), entity.getConsumedAt(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public JpaOtpEntity toEntity(EmailOtp otp) {
        JpaOtpEntity entity = new JpaOtpEntity();
        entity.setId(otp.getId());
        entity.setUserId(otp.getUserId());
        entity.setEmail(otp.getEmail().getValue());
        entity.setOtpHash(otp.getOtpHash());
        entity.setAttemptCount(otp.getAttemptCount());
        entity.setResendCount(otp.getResendCount());
        entity.setLastSentAt(otp.getLastSentAt());
        entity.setExpiresAt(otp.getExpiresAt());
        entity.setConsumedAt(otp.getConsumedAt());
        entity.setCreatedAt(otp.getCreatedAt());
        entity.setUpdatedAt(otp.getUpdatedAt());
        return entity;
    }
}
