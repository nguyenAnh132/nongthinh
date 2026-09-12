package com.nongthinh.profile_service.domain.shared.valueobject;

import java.util.Objects;
import java.util.UUID;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public final class Address {

    private static final int MAX_DETAIL_LENGTH = 500;
    private static final int MAX_PROVINCE_ID_LENGTH = 10;

    private final String provinceId;
    private final UUID communeId;
    private final String detail;

    private Address(String provinceId, UUID communeId, String detail) {
        this.provinceId = provinceId;
        this.communeId = communeId;
        this.detail = detail;
    }

    public static Address of(String provinceId, UUID communeId, String detail) {
        String normalizedProvinceId = normalizeProvinceId(provinceId);
        String normalizedDetail = normalizeDetail(detail);

        boolean hasProvince = normalizedProvinceId != null;
        boolean hasCommune = communeId != null;
        if (hasProvince ^ hasCommune) {
            throw new BusinessException(
                    hasProvince ? ErrorCode.COMMUNE_ID_REQUIRED : ErrorCode.PROVINCE_ID_REQUIRED);
        }

        if (normalizedProvinceId != null && normalizedProvinceId.length() > MAX_PROVINCE_ID_LENGTH) {
            throw new BusinessException(ErrorCode.PROVINCE_ID_INVALID);
        }
        if (normalizedDetail != null && normalizedDetail.length() > MAX_DETAIL_LENGTH) {
            throw new BusinessException(ErrorCode.ADDRESS_DETAIL_INVALID);
        }
        return new Address(normalizedProvinceId, communeId, normalizedDetail);
    }

    public static Address empty() {
        return new Address(null, null, null);
    }

    private static String normalizeProvinceId(String provinceId) {
        if (provinceId == null || provinceId.isBlank()) {
            return null;
        }
        return provinceId.trim();
    }

    private static String normalizeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return null;
        }
        return detail.trim();
    }

    public boolean isEmpty() {
        return provinceId == null && communeId == null && detail == null;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public UUID getCommuneId() {
        return communeId;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address that)) {
            return false;
        }
        return Objects.equals(provinceId, that.provinceId)
            && Objects.equals(communeId, that.communeId)
            && Objects.equals(detail, that.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provinceId, communeId, detail);
    }
}
