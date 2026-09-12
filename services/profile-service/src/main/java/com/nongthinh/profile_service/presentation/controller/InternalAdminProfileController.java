package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.admin.GetAdminProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("internal/admin-profiles")
@RequiredArgsConstructor
public class InternalAdminProfileController {

    private final GetAdminProfileByUserIdUseCase getAdminProfileByUserIdUseCase;

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<AdminProfileView>> getAdminProfileByUserId(@PathVariable UUID userId) {
        AdminProfileView view = getAdminProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin profile retrieved successfully")
                .result(view)
                .build());
    }

}
