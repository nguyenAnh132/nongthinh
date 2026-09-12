package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.admin.GetAdminBrandProfileDetailUseCase;
import com.nongthinh.profile_service.application.view.AdminBrandProfileDetailView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/brand-profiles")
@RequiredArgsConstructor
public class InternalBrandProfileDetailController {

    private final GetAdminBrandProfileDetailUseCase getAdminBrandProfileDetailUseCase;

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<AdminBrandProfileDetailView>> getDetail(@PathVariable UUID id) {
        AdminBrandProfileDetailView view = getAdminBrandProfileDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<AdminBrandProfileDetailView>builder()
                .message("Brand profile detail retrieved successfully")
                .result(view)
                .build());
    }
}
