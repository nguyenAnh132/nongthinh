package com.nongthinh.rice_disease_diagnosis_service.infra;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUser;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;

@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {
    @Override
    public CurrentUser getCurrentUser() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token)) {
            throw new DiagnosisException(ErrorCode.UNAUTHENTICATED);
        }
        String userId = token.getToken().getClaimAsString("nongthinh_id");
        try {
            return new CurrentUser(UUID.fromString(userId));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new DiagnosisException(ErrorCode.UNAUTHENTICATED, ex);
        }
    }
}
