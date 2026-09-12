package com.nongthinh.notification_service.infra;
import com.nongthinh.notification_service.application.port.out.CurrentUserProvider;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {
    public UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt) || !jwt.isAuthenticated())
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        try { return UUID.fromString(jwt.getToken().getClaimAsString("nongthinh_id")); }
        catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, ex);
        }
    }
}
