package com.nongthinh.auth_service.application.service;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.auth_service.application.port.out.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.nongthinh.auth_service.application.event.RegisterOtpRequest;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpGenerator;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.domain.otp.EmailOtp;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.common.constant.DefaultParamValueConstant;
import com.nongthinh.auth_service.common.constant.SystemParamNameConstant;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOtpIssuer {

    private final OtpGenerator otpGenerator;
    private final OtpHash otpHash;
    private final OtpRepository otpRepository;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final SystemParam systemParam;
    private final UserRepository userRepository;

    @Transactional
    public void issueInitial(UUID userId, String email, String userName) {
        String normalizedEmail = Email.normalize(email);
        User user = userRepository.findByEmailForUpdate(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }
        if (otpRepository.findByEmailForUpdate(normalizedEmail).isPresent()) {
            return;
        }
        Instant now = clockProvider.now();
        int otpLength = systemParam.getInt(SystemParamNameConstant.OTP_LENGTH, DefaultParamValueConstant.DEFAULT_OTP_LENGTH);
        int otpExpireMinutes = systemParam.getInt(
                SystemParamNameConstant.OTP_EXPIRE_MINUTES,
                DefaultParamValueConstant.DEFAULT_OTP_EXPIRE_MINUTES);

        String otp = otpGenerator.generate(otpLength);

        EmailOtp record = EmailOtp.issue(idGenerator.generate(), userId, Email.of(normalizedEmail),
                otpHash.hash(normalizedEmail, otp), now, now.plusSeconds(otpExpireMinutes * 60L));
        otpRepository.save(record);
        publishOtpEvent(userId, normalizedEmail, otp, userName, otpExpireMinutes, now);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void issueResend(UUID userId, String email, String userName, EmailOtp existingRecord,
            int maxResend, int cooldownSeconds) {
        String normalizedEmail = Email.normalize(email);
        Instant now = clockProvider.now();
        int otpLength = systemParam.getInt(SystemParamNameConstant.OTP_LENGTH, DefaultParamValueConstant.DEFAULT_OTP_LENGTH);
        int otpExpireMinutes = systemParam.getInt(
                SystemParamNameConstant.OTP_EXPIRE_MINUTES,
                DefaultParamValueConstant.DEFAULT_OTP_EXPIRE_MINUTES);

        String otp = otpGenerator.generate(otpLength);

        existingRecord.resend(otpHash.hash(normalizedEmail, otp), now,
                now.plusSeconds(otpExpireMinutes * 60L), maxResend, cooldownSeconds);
        otpRepository.save(existingRecord);
        publishOtpEvent(userId, normalizedEmail, otp, userName, otpExpireMinutes, now);
    }

    private void publishOtpEvent(
            UUID userId,
            String email,
            String otp,
            String userName,
            int otpExpireMinutes,
            Instant now) {
        RegisterOtpRequest event = new RegisterOtpRequest(
                idGenerator.generate(),
                now,
                userId,
                email,
                otp,
                userName,
                otpExpireMinutes);
        // The consumer must only receive codes whose database transaction has committed.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(event);
            }
        });
    }
}
