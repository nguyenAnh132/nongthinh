package com.nongthinh.auth_service.application.port.in.otp.impl;

import java.util.Objects;
import java.time.Instant;
import com.nongthinh.auth_service.application.port.out.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.auth_service.application.command.VerifyEmailOtpCommand;
import com.nongthinh.auth_service.application.port.in.otp.VerifyEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import com.nongthinh.auth_service.domain.otp.EmailOtp;
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
    private final OtpRepository otpRepository;
    private final OtpHash otpHash;
    private final ClockProvider clockProvider;
    private final SystemParam systemParam;

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void execute(VerifyEmailOtpCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        String normalizedEmail = Email.normalize(command.email());
        User user = userRepository.findByEmailForUpdate(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        int maxAttempts = systemParam.getInt(
                SystemParamNameConstant.OTP_MAX_ATTEMPTS,
                DefaultParamValueConstant.DEFAULT_OTP_MAX_ATTEMPTS);
        EmailOtp record = otpRepository.findByEmailForUpdate(normalizedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_EXPIRED));

        Instant now = clockProvider.now();
        record.ensureCanVerify(now, maxAttempts);

        if (!otpHash.matches(normalizedEmail, command.otp(), record.getOtpHash())) {
            record.recordFailedAttempt(now, maxAttempts);
            otpRepository.save(record);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        record.consume(now, maxAttempts);
        user.completeEmailVerification(now);
        userRepository.save(user);
        otpRepository.save(record);

        log.info(
                "[Application - VerifyEmailOtp] Email verified successfully | userId={} durationMs={}",
                user.getId(),
                System.currentTimeMillis() - start
        );
    }
}
