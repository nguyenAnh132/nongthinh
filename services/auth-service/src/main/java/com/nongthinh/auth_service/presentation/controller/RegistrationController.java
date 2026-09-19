package com.nongthinh.auth_service.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.port.in.user.CompleteRegistrationUseCase;
import com.nongthinh.auth_service.application.view.CompleteRegistrationView;
import com.nongthinh.auth_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.presentation.dto.request.CompleteRegistrationRequest;
import com.nongthinh.auth_service.presentation.mapper.RegistrationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RegistrationController {
    private final CompleteRegistrationUseCase completeRegistration;
    private final CurrentUserProvider currentUser;
    private final RegistrationMapper mapper;

    @PostMapping("/me/complete-registration")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FARMER', 'ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<CompleteRegistrationView>> complete(@RequestBody @Valid CompleteRegistrationRequest request) {
        var result = completeRegistration.execute(currentUser.getRegistrationPrincipal(), mapper.toCommand(request));
        return ResponseEntity.ok(ApiResponse.<CompleteRegistrationView>builder()
                .message("Registration completed successfully").result(result).build());
    }
}
