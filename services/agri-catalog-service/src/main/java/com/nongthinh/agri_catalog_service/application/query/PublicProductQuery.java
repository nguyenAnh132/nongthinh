package com.nongthinh.agri_catalog_service.application.query;

import java.util.Locale;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

public record PublicProductQuery(String name, String diseaseName, String keyword, int page) {
    public static final int PAGE_SIZE = 10;

    public PublicProductQuery(String name, String diseaseName, int page) {
        this(name, diseaseName, "", page);
    }

    public PublicProductQuery {
        name = normalize(name);
        diseaseName = normalize(diseaseName);
        keyword = normalize(keyword);
        if (page < 0 || page > Integer.MAX_VALUE / PAGE_SIZE || name.length() > 255
                || diseaseName.length() > 255 || keyword.length() > 255) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
