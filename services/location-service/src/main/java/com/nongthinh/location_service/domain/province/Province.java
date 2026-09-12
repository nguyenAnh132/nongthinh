package com.nongthinh.location_service.domain.province;

import java.util.Objects;

public final class Province {

    private final String id;
    private String code;
    private String name;

    private Province(String id, String code, String name) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
    }

    public static Province create(String id, String code, String name) {
        return new Province(id, code, name);
    }

    public static Province reconstruct(String id, String code, String name) {
        return new Province(id, code, name);
    }

    public void updateCode(String code) {
        this.code = Objects.requireNonNull(code, "code is required");
    }

    public void updateName(String name) {
        this.name = Objects.requireNonNull(name, "name is required");
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
