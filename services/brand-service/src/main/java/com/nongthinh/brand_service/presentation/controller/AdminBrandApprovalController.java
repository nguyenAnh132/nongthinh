package com.nongthinh.brand_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.brand_service.application.port.in.workflow.GetBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.view.BrandApprovalProcessView;
import com.nongthinh.brand_service.common.constant.PermissionConstant;
import com.nongthinh.brand_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/brand-profiles")
@RequiredArgsConstructor
public class AdminBrandApprovalController {

    private final GetBrandApprovalProcessUseCase getBrandApprovalProcessUseCase;

    @GetMapping("/{brandProfileId}/process")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstant.BRAND_APPROVE + "', '" + PermissionConstant.TICKET_MANAGE
            + "')")
    public ResponseEntity<ApiResponse<BrandApprovalProcessView>> getProcess(
            @PathVariable UUID brandProfileId) {
        BrandApprovalProcessView view = getBrandApprovalProcessUseCase.execute(brandProfileId);
        return ResponseEntity.ok(ApiResponse.<BrandApprovalProcessView>builder()
                .message("Brand approval process retrieved successfully")
                .result(view)
                .build());
    }
}
