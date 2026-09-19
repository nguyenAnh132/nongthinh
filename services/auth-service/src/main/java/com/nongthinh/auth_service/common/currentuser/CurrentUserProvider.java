package com.nongthinh.auth_service.common.currentuser;

public interface CurrentUserProvider {
    CurrentUser getCurrentUser();

    com.nongthinh.auth_service.application.view.RegistrationPrincipal getRegistrationPrincipal();
}
