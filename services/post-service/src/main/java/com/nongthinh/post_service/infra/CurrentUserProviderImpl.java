package com.nongthinh.post_service.infra;

import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public final class CurrentUserProviderImpl implements CurrentUserProvider {
    private static final String USER_ID_CLAIM = "nongthinh_id";

    @Override
    public Optional<UUID> findCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String value = jwtAuthentication.getToken().getClaimAsString(USER_ID_CLAIM);
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, ex);
        }
    }
}
