package com.nongthinh.bo_portal_service.configuration;

import com.nongthinh.bo_portal_service.application.port.out.BrandAccessQuery;
import com.nongthinh.bo_portal_service.application.view.BrandAccessView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BrandAccessProperties.class)
public class BrandAccessConfig implements WebMvcConfigurer, HandlerInterceptor {
    private static final String BRAND_ROLE = "ROLE_BRAND";
    private static final String PENDING_ROLE = "ROLE_BRAND_PENDING";
    private final BrandAccessQuery brandAccess;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return true;
        boolean brand = authentication.getAuthorities().stream().anyMatch(a -> BRAND_ROLE.equals(a.getAuthority()));
        boolean pending = authentication.getAuthorities().stream().anyMatch(a -> PENDING_ROLE.equals(a.getAuthority()));
        if (!brand && !pending) return true;
        String path = request.getServletPath();
        String method = request.getMethod();
        BrandAccessView access = brandAccess.getCurrentAccess();
        if (brand && access.active()) return true;
        if (isRegistrationRequest(request, path, method, access)) return true;
        throw new BusinessException(ErrorCode.BRAND_ACCESS_DENIED);
    }

    private boolean isRegistrationRequest(HttpServletRequest request, String path, String method, BrandAccessView access) {
        return "GET".equals(method) && path.matches("/upload-policies/(BRAND_LOGO|BRAND_BANNER|BUSINESS_LICENSE)");
    }
}
