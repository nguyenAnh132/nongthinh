package com.nongthinh.auth_service.common.constant;

import java.util.Set;

public final class AdminGroupConstant {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String OPERATION = "OPERATION";

    private static final Set<String> ADMIN_GROUPS = Set.of(
            SUPER_ADMIN,
            OPERATION
    );

    private AdminGroupConstant() {
    }

    public static boolean isAdminGroup(String authority) {
        return authority != null && ADMIN_GROUPS.contains(authority);
    }

    public static Set<String> all() {
        return ADMIN_GROUPS;
    }
}
