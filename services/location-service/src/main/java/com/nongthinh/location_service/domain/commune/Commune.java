package com.nongthinh.location_service.domain.commune;

import java.util.Objects;
import java.util.UUID;

public final class Commune {

    private final UUID id;
    private String provinceId;
    private String code;
    private String name;

    private Commune(UUID id, String provinceId, String code, String name) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.provinceId = Objects.requireNonNull(provinceId, "provinceId is required");
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
    }

    public static Commune create(UUID id, String provinceId, String code, String name) {
        return new Commune(id, provinceId, code, name);
    }

    public static Commune reconstruct(UUID id, String provinceId, String code, String name) {
        return new Commune(id, provinceId, code, name);
    }

    public void updateProvinceId(String provinceId) {
        this.provinceId = Objects.requireNonNull(provinceId, "provinceId is required");
    }

    public void updateCode(String code) {
        this.code = Objects.requireNonNull(code, "code is required");
    }

    public void updateName(String name) {
        this.name = Objects.requireNonNull(name, "name is required");
    }

    public UUID getId() {
        return id;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
