package com.nongthinh.auth_service.application.port.out.otp;

public interface OtpHash {

    String hash(String email, String rawOtp);

    boolean matches(String email, String rawOtp, String hashedOtp);
}
