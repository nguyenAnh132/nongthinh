package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.admin.UpdateAdminProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateBrandProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateFarmerProfileStatusUseCase;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.admin.BrandProfileStatusUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.admin.ProfileStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminProfileManagementController {

    private final UpdateFarmerProfileStatusUseCase updateFarmerProfileStatusUseCase;
    private final UpdateBrandProfileStatusUseCase updateBrandProfileStatusUseCase;
    private final UpdateAdminProfileStatusUseCase updateAdminProfileStatusUseCase;

    @PatchMapping("/farmer-profiles/{id}/status")
    public ResponseEntity<ApiResponse<FarmerProfileView>> updateFarmerProfileStatus(
            @PathVariable UUID id,
            @RequestBody @Valid ProfileStatusUpdateRequest request
    ) {
        FarmerProfileView view = updateFarmerProfileStatusUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer profile status updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/brand-profiles/{id}/status")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateBrandProfileStatus(
            @PathVariable UUID id,
            @RequestBody @Valid BrandProfileStatusUpdateRequest request
    ) {
        BrandProfileView view = updateBrandProfileStatusUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile status updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/admin-profiles/{id}/status")
    public ResponseEntity<ApiResponse<AdminProfileView>> updateAdminProfileStatus(
            @PathVariable UUID id,
            @RequestBody @Valid ProfileStatusUpdateRequest request
    ) {
        AdminProfileView view = updateAdminProfileStatusUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin profile status updated successfully")
                .result(view)
                .build());
    }
}
