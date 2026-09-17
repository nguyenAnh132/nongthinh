package com.nongthinh.file_service.common.constant;

import java.util.Set;

public final class RoleConstant {

    public static final String ROLE_FARMER = "ROLE_FARMER";
    public static final String ROLE_BRAND = "ROLE_BRAND";
    public static final String ROLE_BRAND_PENDING = "ROLE_BRAND_PENDING";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_INTERNAL = "ROLE_INTERNAL";

    private static final Set<String> APPLICATION_ROLES = Set.of(
            ROLE_FARMER,
            ROLE_BRAND,
            ROLE_BRAND_PENDING,
            ROLE_ADMIN);

    private RoleConstant() {
    }

    public static boolean isApplicationRole(String authority) {
        return APPLICATION_ROLES.contains(authority);
    }
}
