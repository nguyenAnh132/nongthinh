package com.nongthinh.auth_service.presentation.controller;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.port.in.auth.GetMeUseCase;
import com.nongthinh.auth_service.application.port.in.auth.LogoutUseCase;
import com.nongthinh.auth_service.application.view.MeView;
import com.nongthinh.auth_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.auth_service.common.cookie.CookieWriter;
import com.nongthinh.auth_service.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.auth_service.application.view.RefreshTokenView;
import com.nongthinh.auth_service.application.port.in.auth.RefreshToken;   
import com.nongthinh.auth_service.common.cookie.RefreshTokenResolver;
import com.nongthinh.auth_service.common.cookie.TokenCookie;

@RequiredArgsConstructor
@RestController
@Slf4j
public class AuthController {

    private final GetMeUseCase getMeUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final RefreshToken refreshTokenUseCase;
    private final CookieWriter cookieWriter;
    private final RefreshTokenResolver refreshTokenResolver;
    private final LogoutUseCase logoutUseCase;

    @GetMapping("login")
    public void login(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/keycloak");
    }

    @PostMapping("refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        log.info("Refresh");
        String refreshToken = refreshTokenResolver.resolve(request);
        RefreshTokenView refreshTokenView = refreshTokenUseCase.execute(refreshToken);
        TokenCookie tokenCookie = new TokenCookie(
            refreshTokenView.accessToken(),
            refreshTokenView.refreshToken(),
            refreshTokenView.expiresIn(),
            refreshTokenView.refreshExpiresIn()
        );
        cookieWriter.setTokenToCookie(response, tokenCookie);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Refresh token retrieved successfully")
                .build());
    }

    @GetMapping("logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = refreshTokenResolver.resolve(request);
        logoutUseCase.execute(refreshToken);
        cookieWriter.clearTokenFromCookie(response);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build());
    }

    @GetMapping("me")
    public ResponseEntity<ApiResponse<MeView>> getMe(
            Authentication authentication
    ) {
        MeView meView = getMeUseCase.execute(currentUserProvider.getRegistrationPrincipal());

        return ResponseEntity.ok(ApiResponse.<MeView>builder()
                .message("Current user retrieved successfully")
                .result(meView)
                .build());
    }

}
