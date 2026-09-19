package com.nongthinh.auth_service.application.command;

import java.util.Set;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.valueobject.Email;

public record CompleteRegistrationCommand(String firstName, String lastName, String gender, String phone,
        String brandName, String representativeName, String representativePhone, String representativeEmail) {

    public void validateFor(String role) {
        if (phone == null || !phone.matches("[0-9]{10}")) invalid();
        switch (role) {
            case RoleConstant.ROLE_ADMIN, RoleConstant.ROLE_FARMER -> {
                if (!personName(firstName, 1) || !personName(lastName, 1)) invalid();
                if (RoleConstant.ROLE_FARMER.equals(role) && (gender == null || !Set.of("MALE", "FEMALE", "OTHER").contains(gender))) invalid();
            }
            case RoleConstant.ROLE_BRAND, RoleConstant.ROLE_BRAND_PENDING -> {
                if (brandName == null || brandName.isBlank() || brandName.trim().length() > 200
                        || !brandName.trim().matches("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s&.,\\-'\"()]*$")
                        || !personName(representativeName, 1) || representativePhone == null
                        || !representativePhone.matches("[0-9]{10}")) invalid();
                Email.of(representativeEmail);
                if (representativeEmail.length() > 255) invalid();
            }
            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean personName(String value, int min) {
        return value != null && value.trim().length() >= min && value.trim().length() <= 200
                && value.trim().matches("[\\p{L}]+(?:[\\s'\\-][\\p{L}]+)*");
    }

    private void invalid() {
        throw new BusinessException(ErrorCode.REGISTRATION_PROFILE_INVALID);
    }
}
