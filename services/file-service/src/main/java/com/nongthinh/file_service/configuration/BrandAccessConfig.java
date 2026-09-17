package com.nongthinh.file_service.configuration;

import com.nongthinh.file_service.application.port.out.BrandAccessQuery;
import com.nongthinh.file_service.application.view.BrandAccessView;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
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
    private final com.nongthinh.file_service.application.port.in.file.GetFileByIdUseCase getFileByIdUseCase;

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
        String purpose = request.getParameter("purpose");
        if ("POST".equals(method) && "/upload".equals(path)) {
            return canUpload(access, purpose);
        }
        if ("GET".equals(method) && "/me".equals(path)) return isProfileFile(purpose);
        if ("GET".equals(method) && path.matches("/(public/)?[0-9a-fA-F-]{36}(/content)?")) {
            String id = path.replaceFirst("^/public/", "/").split("/")[1];
            // The use case checks ownership before exposing the file's purpose.
            var view = getFileByIdUseCase.execute(java.util.UUID.fromString(id));
            return isProfileFile(view.purpose().name());
        }
        return false;
    }

    private boolean isProfileFile(String purpose) {
        return "BRAND_LOGO".equals(purpose) || "BRAND_BANNER".equals(purpose) || "BUSINESS_LICENSE".equals(purpose);
    }

    private boolean canUpload(BrandAccessView access, String purpose) {
        if ("BUSINESS_LICENSE".equals(purpose)) return access.canSubmitDocuments();
        return access.canEditProfile() && ("BRAND_LOGO".equals(purpose) || "BRAND_BANNER".equals(purpose));
    }
}
