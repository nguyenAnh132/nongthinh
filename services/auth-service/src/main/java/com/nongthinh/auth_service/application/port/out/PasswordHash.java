package com.nongthinh.auth_service.application.port.out;

public interface PasswordHash {

    String hash(String email, String rawPassword);

    boolean matches(String email, String rawPassword, String hashedPassword);
}
