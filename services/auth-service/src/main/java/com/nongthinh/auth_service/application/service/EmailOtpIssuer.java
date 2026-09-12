package com.nongthinh.auth_service.application.service;

import java.time.Instant;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.event.RegisterOtpRequest;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpGenerator;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import com.nongthinh.auth_service.application.port.out.otp.OtpRecord;
import com.nongthinh.auth_service.application.port.out.otp.OtpStore;
import com.nongthinh.auth_service.common.constant.DefaultParamValueConstant;
import com.nongthinh.auth_service.common.constant.SystemParamNameConstant;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOtpIssuer {

    private final OtpGenerator otpGenerator;
    private final OtpHash otpHash;
    private final OtpStore otpStore;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final SystemParam systemParam;

    public void issueInitial(UUID userId, String email, String userName) {
        String normalizedEmail = Email.normalize(email);
        Instant now = clockProvider.now();
        int otpLength = systemParam.getInt(SystemParamNameConstant.OTP_LENGTH, DefaultParamValueConstant.DEFAULT_OTP_LENGTH);
        int otpExpireMinutes = systemParam.getInt(
                SystemParamNameConstant.OTP_EXPIRE_MINUTES,
                DefaultParamValueConstant.DEFAULT_OTP_EXPIRE_MINUTES);

        String otp = otpGenerator.generate(otpLength);
        log.info("OTP: {}", otp);
        OtpRecord record = OtpRecord.initial(otpHash.hash(normalizedEmail, otp), now);
        otpStore.put(normalizedEmail, record, otpExpireMinutes);
        publishOtpEvent(userId, normalizedEmail, otp, userName, otpExpireMinutes, now);
    }

    public void issueResend(UUID userId, String email, String userName, OtpRecord existingRecord) {
        String normalizedEmail = Email.normalize(email);
        Instant now = clockProvider.now();
        int otpLength = systemParam.getInt(SystemParamNameConstant.OTP_LENGTH, DefaultParamValueConstant.DEFAULT_OTP_LENGTH);
        int otpExpireMinutes = systemParam.getInt(
                SystemParamNameConstant.OTP_EXPIRE_MINUTES,
                DefaultParamValueConstant.DEFAULT_OTP_EXPIRE_MINUTES);

        String otp = otpGenerator.generate(otpLength);
        log.info("OTP: {}", otp);
        OtpRecord record = existingRecord.nextResend(otpHash.hash(normalizedEmail, otp), now);
        otpStore.put(normalizedEmail, record, otpExpireMinutes);
        publishOtpEvent(userId, normalizedEmail, otp, userName, otpExpireMinutes, now);
    }

    private void publishOtpEvent(
            UUID userId,
            String email,
            String otp,
            String userName,
            int otpExpireMinutes,
            Instant now) {
        eventPublisher.publish(new RegisterOtpRequest(
                idGenerator.generate(),
                now,
                userId,
                email,
                otp,
                userName,
                otpExpireMinutes));
    }
}
