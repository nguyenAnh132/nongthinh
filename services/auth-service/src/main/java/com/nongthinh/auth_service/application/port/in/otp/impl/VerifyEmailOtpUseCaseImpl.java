package com.nongthinh.auth_service.application.port.in.otp.impl;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.command.VerifyEmailOtpCommand;
import com.nongthinh.auth_service.application.port.in.otp.VerifyEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import com.nongthinh.auth_service.application.port.out.otp.OtpRecord;
import com.nongthinh.auth_service.application.port.out.otp.OtpStore;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
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
public class VerifyEmailOtpUseCaseImpl implements VerifyEmailOtpUseCase {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final OtpHash otpHash;
    private final ClockProvider clockProvider;
    private final SystemParam systemParam;

    @Override
    public void execute(VerifyEmailOtpCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        String normalizedEmail = Email.normalize(command.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        int maxAttempts = systemParam.getInt(
                SystemParamNameConstant.OTP_MAX_ATTEMPTS,
                DefaultParamValueConstant.DEFAULT_OTP_MAX_ATTEMPTS);
        int otpExpireMinutes = systemParam.getInt(
                SystemParamNameConstant.OTP_EXPIRE_MINUTES,
                DefaultParamValueConstant.DEFAULT_OTP_EXPIRE_MINUTES);

        OtpRecord record = otpStore.find(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_EXPIRED));

        if (record.attemptCount() >= maxAttempts) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED);
        }

        if (!otpHash.matches(normalizedEmail, command.otp(), record.otpHash())) {
            otpStore.incrementAttempt(normalizedEmail, otpExpireMinutes);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        user.completeEmailVerification(clockProvider.now());
        userRepository.save(user);
        otpStore.invalidate(normalizedEmail);

        log.info(
                "[Application - VerifyEmailOtp] OTP verification code sent successfully | userId={} durationMs={}",
                user.getId(),
                System.currentTimeMillis() - start
        );
    }
}
