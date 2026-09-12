package com.nongthinh.auth_service.application.port.out;

public interface SystemParam {

    String getString(String name, String defaultValue);

    int getInt(String name, int defaultValue);

    boolean getBoolean(String name, boolean defaultValue);
}
