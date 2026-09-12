package com.nongthinh.auth_service.application.command;

public record RegisterAdminCommand(
    String email,
    String password,
    boolean temporary,
    boolean enabled,
    String firstName,
    String lastName,
    String phone,
    String adminGroup,
    String avatarUrl
) {
}
