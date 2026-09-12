package com.nongthinh.brand_service.common.constant;

import java.util.LinkedHashSet;
import java.util.Set;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;

public final class PermissionConstant {

    public static final String BRAND_APPROVE = "brand:approve";
    public static final String TICKET_MANAGE = "ticket:manage";
    public static final String BRAND_VERIFY = "brand:verify";
    public static final String USER_READ = "user:read";
    public static final String CONTENT_MODERATE = "content:moderate";
    public static final String SYSTEM_CONFIG_READ = "system:config:read";
    public static final String SYSTEM_CONFIG_WRITE = "system:config:write";
    public static final String NOTIFICATION_EMAIL_MANAGE = "notification:email:manage";
    public static final String ADMIN_USER_READ = "admin:user:read";
    public static final String ADMIN_USER_WRITE = "admin:user:write";
    public static final String ADMIN_ROLE_MANAGE = "admin:role:manage";
    public static final String SUPPORT_CUSTOMER = "support:customer";
    public static final String INCIDENT_MANAGE = "incident:manage";

    public static final Set<String> OPERATION_PERMISSIONS = Set.of(
            TICKET_MANAGE,
            BRAND_APPROVE,
            BRAND_VERIFY,
            USER_READ,
            CONTENT_MODERATE
    );

    public static final Set<String> SUPER_ADMIN_PERMISSIONS = Set.of(
            BRAND_APPROVE,
            BRAND_VERIFY,
            USER_READ,
            CONTENT_MODERATE,
            TICKET_MANAGE,
            SYSTEM_CONFIG_READ,
            SYSTEM_CONFIG_WRITE,
            NOTIFICATION_EMAIL_MANAGE,
            ADMIN_USER_READ,
            ADMIN_USER_WRITE,
            ADMIN_ROLE_MANAGE,
            SUPPORT_CUSTOMER,
            INCIDENT_MANAGE
    );

    private PermissionConstant() {
    }

    public static boolean isPermission(String authority) {
        return authority != null && authority.contains(":");
    }

    public static Set<String> forAdminGroup(String adminGroup) {
        if (AdminGroupConstant.SUPER_ADMIN.equals(adminGroup)) {
            return SUPER_ADMIN_PERMISSIONS;
        }
        if (AdminGroupConstant.OPERATION.equals(adminGroup)) {
            return OPERATION_PERMISSIONS;
        }
        throw new BusinessException(ErrorCode.ADMIN_GROUP_INVALID);
    }

    public static Set<String> realmRolesForAdminGroup(String adminGroup) {
        Set<String> roles = new LinkedHashSet<>();
        roles.add(RoleConstant.ROLE_ADMIN);
        roles.add(adminGroup);
        roles.addAll(forAdminGroup(adminGroup));
        return roles;
    }
}
