package com.nongthinh.auth_service.infra.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpHashImpl implements OtpHash {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String email, String rawOtp) {
        return passwordEncoder.encode(email + ":otp:" + rawOtp);
    }

    @Override
    public boolean matches(String email, String rawOtp, String hashedOtp) {
        return passwordEncoder.matches(email + ":otp:" + rawOtp, hashedOtp);
    }
}
