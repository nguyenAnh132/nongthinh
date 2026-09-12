package com.nongthinh.auth_service.infra.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.port.out.PasswordHash;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordHashImpl implements PasswordHash {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String email, String rawPassword) {
        return passwordEncoder.encode(email + rawPassword);
    }

    @Override
    public boolean matches(String email, String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(email + rawPassword, hashedPassword);
    }
}
