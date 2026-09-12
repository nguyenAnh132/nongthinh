package com.nongthinh.auth_service.application.port.in.otp.impl;

import java.time.Duration;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.command.ResendEmailOtpCommand;
import com.nongthinh.auth_service.application.port.in.otp.ResendEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpRecord;
import com.nongthinh.auth_service.application.port.out.otp.OtpStore;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.service.EmailOtpIssuer;
import com.nongthinh.auth_service.application.view.OtpResendCooldownView;
import com.nongthinh.auth_service.common.constant.DefaultParamValueConstant;
import com.nongthinh.auth_service.common.constant.SystemParamNameConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendEmailOtpUseCaseImpl implements ResendEmailOtpUseCase {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final EmailOtpIssuer emailOtpIssuer;
    private final ClockProvider clockProvider;
    private final SystemParam systemParam;

    @Override
    public OtpResendCooldownView execute(ResendEmailOtpCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        String normalizedEmail = Email.normalize(command.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        int maxResend = systemParam.getInt(
                SystemParamNameConstant.OTP_MAX_RESEND,
                DefaultParamValueConstant.DEFAULT_OTP_MAX_RESEND);
        int cooldownSeconds = systemParam.getInt(
                SystemParamNameConstant.OTP_RESEND_COOLDOWN_SECONDS,
                DefaultParamValueConstant.DEFAULT_OTP_RESEND_COOLDOWN_SECONDS);

        OtpRecord existingRecord = otpStore.find(normalizedEmail).orElse(null);
        if (existingRecord != null) {
            if (existingRecord.resendCount() >= maxResend) {
                throw new BusinessException(ErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
            }

            long elapsedSeconds = Duration.between(existingRecord.lastSentAt(), clockProvider.now()).getSeconds();
            if (elapsedSeconds < cooldownSeconds) {
                throw new BusinessException(ErrorCode.OTP_RESEND_TOO_FREQUENT);
            }

            emailOtpIssuer.issueResend(
                    user.getId(),
                    normalizedEmail,
                    extractLocalPart(normalizedEmail),
                    existingRecord);
        } else {
            emailOtpIssuer.issueInitial(
                    user.getId(),
                    normalizedEmail,
                    extractLocalPart(normalizedEmail));
        }

        log.info(
                "[Application - ResendEmailOtp] OTP verification code successfully resent | userId={} durationMs={}",
                user.getId(),
                System.currentTimeMillis() - start
        );

        return new OtpResendCooldownView(cooldownSeconds);
    }

    private static String extractLocalPart(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        return email.substring(0, atIndex);
    }
}
