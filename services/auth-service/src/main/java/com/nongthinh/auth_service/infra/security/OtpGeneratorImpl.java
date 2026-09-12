package com.nongthinh.auth_service.infra.security;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.port.out.otp.OtpGenerator;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;

@Component
public class OtpGeneratorImpl implements OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate(int length) {
        if (length <= 0) {
            throw new BusinessException(ErrorCode.OTP_LENGTH_INVALID);
        }

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }
}
