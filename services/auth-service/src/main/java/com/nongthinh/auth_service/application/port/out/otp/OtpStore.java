package com.nongthinh.auth_service.application.port.out.otp;

import java.util.Optional;

public interface OtpStore {

    void put(String email, OtpRecord record, int expireMinutes);

    Optional<OtpRecord> find(String email);

    void incrementAttempt(String email, int expireMinutes);

    void invalidate(String email);
}
