package com.nongthinh.post_service.application.port.out;

import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;

public interface CurrentUserProvider {
    Optional<UUID> findCurrentUserId();

    default UUID getCurrentUserId() {
        return findCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
    }
}
