package com.nongthinh.auth_service.application.exception;

import com.nongthinh.auth_service.application.view.RegistrationRequiredView;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;

public class RegistrationRequiredException extends BusinessException {
    private final RegistrationRequiredView registration;

    public RegistrationRequiredException(String email, String role) {
        super(ErrorCode.REGISTRATION_REQUIRED);
        this.registration = new RegistrationRequiredView(email, role);
    }

    public RegistrationRequiredView getRegistration() {
        return registration;
    }
}
