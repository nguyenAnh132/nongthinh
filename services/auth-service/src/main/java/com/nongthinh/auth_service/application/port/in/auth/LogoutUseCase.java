package com.nongthinh.auth_service.application.port.in.auth;

public interface LogoutUseCase {

    void execute(String refreshToken);

}
