package com.nongthinh.post_service.configuration;

import com.nongthinh.post_service.application.port.out.BrandAccessQuery;
import com.nongthinh.post_service.application.view.BrandAccessView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrandAccessConfigTest {
    private final BrandAccessQuery access = mock(BrandAccessQuery.class);
    private final BrandAccessConfig config = new BrandAccessConfig(access );

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_BRAND", "ROLE_BRAND_PENDING"})
    void blocksBusinessEndpointsWithNewAndOldTokens(String role) {
        authenticate(role);
        stub(false, true, true);
        var error = assertThrows(BusinessException.class, () -> check("GET", "/feature"));
        assertEquals(ErrorCode.BRAND_ACCESS_DENIED, error.getErrorCode());
        assertThrows(BusinessException.class, () -> check("POST", "/feature"));
    }

    @Test
    void requiresBothActiveProfileAndBrandRole() {
        stub(true, true, false);
        authenticate("ROLE_BRAND_PENDING");
        assertThrows(BusinessException.class, () -> check("GET", "/feature"));
        authenticate("ROLE_BRAND");
        assertTrue(check("POST", "/feature"));
    }

    @Test
    void statusChangeTakesEffectWithoutTokenExpiry() {
        authenticate("ROLE_BRAND");
        stub(true, true, false);
        assertTrue(check("GET", "/feature"));
        stub(false, false, false);
        assertThrows(BusinessException.class, () -> check("GET", "/feature"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_FARMER", "ROLE_ADMIN", "ROLE_INTERNAL"})
    void preservesOtherIdentities(String role) {
        authenticate(role);
        assertTrue(check("GET", "/feature"));
        verifyNoInteractions(access);
    }

    @Test
    void failsClosedWhenStatusCannotBeVerified() {
        authenticate("ROLE_BRAND");
        when(access.getCurrentAccess()).thenThrow(new IllegalStateException("Unavailable"));
        assertThrows(IllegalStateException.class, () -> check("GET", "/feature"));
    }


    private void stub(boolean active, boolean editable, boolean documents) {
        when(access.getCurrentAccess()).thenReturn(new BrandAccessView(active, editable, documents));
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority(role))));
    }

    private boolean check(String method, String path) {
        var request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return config.preHandle(request, new MockHttpServletResponse(), new Object());
    }
}
