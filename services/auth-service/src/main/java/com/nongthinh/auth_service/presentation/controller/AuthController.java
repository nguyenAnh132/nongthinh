package com.nongthinh.auth_service.presentation.controller;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.command.ResendEmailOtpCommand;
import com.nongthinh.auth_service.application.command.VerifyEmailOtpCommand;
import com.nongthinh.auth_service.application.port.in.auth.GetMeUseCase;
import com.nongthinh.auth_service.application.port.in.auth.LogoutUseCase;
import com.nongthinh.auth_service.application.port.in.otp.GetOtpConfigUseCase;
import com.nongthinh.auth_service.application.port.in.otp.ResendEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.in.otp.VerifyEmailOtpUseCase;
import com.nongthinh.auth_service.application.view.MeView;
import com.nongthinh.auth_service.application.view.OtpConfigView;
import com.nongthinh.auth_service.application.view.OtpResendCooldownView;
import com.nongthinh.auth_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.auth_service.common.cookie.CookieWriter;
import com.nongthinh.auth_service.common.currentuser.CurrentUser;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.presentation.dto.request.ResendEmailOtpRequest;
import com.nongthinh.auth_service.presentation.dto.request.VerifyEmailOtpRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    private final VerifyEmailOtpUseCase verifyEmailOtpUseCase;
    private final ResendEmailOtpUseCase resendEmailOtpUseCase;
    private final GetOtpConfigUseCase getOtpConfigUseCase;
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
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        MeView meView = getMeUseCase.execute(
            currentUser.getUserId(),
            currentUser.getRoles(),
            currentUser.getAdminGroup(),
            currentUser.getPermissions()
        );

        return ResponseEntity.ok(ApiResponse.<MeView>builder()
                .message("Current user retrieved successfully")
                .result(meView)
                .build());
    }

    @PostMapping("otp/verify")
    public ResponseEntity<Void> verifyEmailOtp(@RequestBody @Valid VerifyEmailOtpRequest request) {
        verifyEmailOtpUseCase.execute(new VerifyEmailOtpCommand(request.email(), request.otp()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("otp/config")
    public ResponseEntity<ApiResponse<OtpConfigView>> getOtpConfig() {
        OtpConfigView view = getOtpConfigUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<OtpConfigView>builder()
                .message("OTP config retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("otp/resend")
    public ResponseEntity<ApiResponse<OtpResendCooldownView>> resendEmailOtp(
            @RequestBody @Valid ResendEmailOtpRequest request) {
        OtpResendCooldownView view = resendEmailOtpUseCase.execute(new ResendEmailOtpCommand(request.email()));
        return ResponseEntity.ok(ApiResponse.<OtpResendCooldownView>builder()
                .message("OTP resent successfully")
                .result(view)
                .build());
    }
}
