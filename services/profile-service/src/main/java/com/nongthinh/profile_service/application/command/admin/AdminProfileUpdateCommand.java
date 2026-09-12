package com.nongthinh.profile_service.application.command.admin;

public record AdminProfileUpdateCommand(
    String firstName,
    String lastName
) {
}
