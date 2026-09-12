package com.nongthinh.auth_service.infra.caching.systemparam;

public final class SystemParamCacheKeys {

    private static final String PREFIX = "auth:system-param:";

    private SystemParamCacheKeys() {
    }

    public static String forParam(String name) {
        return PREFIX + name;
    }
}
