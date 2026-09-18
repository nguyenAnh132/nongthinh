package com.nongthinh.profile_service.configuration;

import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandAccessUseCase;
import com.nongthinh.profile_service.application.view.BrandAccessView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
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
    private final GetMyBrandAccessUseCase access = mock(GetMyBrandAccessUseCase.class);
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
        when(access.execute()).thenThrow(new IllegalStateException("Unavailable"));
        assertThrows(IllegalStateException.class, () -> check("GET", "/feature"));
    }


    @Test
    void rejectedAccountHasReadOnlyProfile() {
        authenticate("ROLE_BRAND_PENDING");
        stub(false, false, false);
        assertTrue(check("GET", "/brand-profiles/me"));
        assertTrue(check("GET", "/brand-profiles/me/documents"));
        assertThrows(BusinessException.class, () -> check("PATCH", "/brand-profiles/me"));
        assertThrows(BusinessException.class, () -> check("PATCH", "/brand-profiles/me/logo"));
        assertThrows(BusinessException.class, () -> check("POST", "/brand-profiles/me/documents"));
    }

    @Test
    void revisionAllowsOnlyOwnRegistrationEndpoints() {
        authenticate("ROLE_BRAND_PENDING");
        stub(false, true, true);
        assertTrue(check("PATCH", "/brand-profiles/me"));
        assertTrue(check("POST", "/brand-profiles/me/documents"));
        assertTrue(check("POST", "/brand-profiles/me/documents-submitted"));
        assertThrows(BusinessException.class, () -> check("POST", "/users/another/follow"));
        assertThrows(BusinessException.class, () -> check("GET", "/brand-profiles/another"));
    }

    @Test
    void accessEndpointDoesNotRecursivelyCheckItself() {
        authenticate("ROLE_BRAND_PENDING");
        assertTrue(check("GET", "/brand-profiles/me/access"));
        verifyNoInteractions(access);
    }

    private void stub(boolean active, boolean editable, boolean documents) {
        when(access.execute()).thenReturn(new BrandAccessView(active, editable, documents));
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
