package com.nongthinh.profile_service.domain.branddocument.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum BrandDocumentReviewStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    NEEDS_REVISION;

    public static BrandDocumentReviewStatus fromString(String raw) {
        if (raw != null) {
            try {
                return BrandDocumentReviewStatus.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.BRAND_DOCUMENT_REVIEW_STATUS_INVALID, e);
            }
        }
        throw new BusinessException(ErrorCode.BRAND_DOCUMENT_REVIEW_STATUS_INVALID);
    }

    public String getValue() {
        return name();
    }
}
