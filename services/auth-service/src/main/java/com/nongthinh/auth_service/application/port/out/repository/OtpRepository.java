package com.nongthinh.auth_service.application.port.out.repository;

import com.nongthinh.auth_service.domain.otp.EmailOtp;
import java.util.Optional;

public interface OtpRepository {

    EmailOtp save(EmailOtp otp);

    Optional<EmailOtp> findByEmail(String email);

    Optional<EmailOtp> findByEmailForUpdate(String email);
}
