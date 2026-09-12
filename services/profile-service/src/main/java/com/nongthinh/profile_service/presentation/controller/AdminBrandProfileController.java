package com.nongthinh.profile_service.presentation.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.admin.GetAdminBrandProfileDetailUseCase;
import com.nongthinh.profile_service.application.port.in.admin.ListBrandProfilesForAdminUseCase;
import com.nongthinh.profile_service.application.port.in.admin.RejectBrandEarlyUseCase;
import com.nongthinh.profile_service.application.view.AdminBrandProfileDetailView;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.admin.RejectBrandEarlyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/brand-profiles")
@RequiredArgsConstructor
public class AdminBrandProfileController {

    private static final List<String> DEFAULT_APPROVAL_STATUSES = List.of(
            "PENDING_APPROVAL",
            "UNDER_REVIEW",
            "NEEDS_REVISION",
            "READY_FOR_FINAL_REVIEW"
    );

    private final ListBrandProfilesForAdminUseCase listBrandProfilesForAdminUseCase;
    private final GetAdminBrandProfileDetailUseCase getAdminBrandProfileDetailUseCase;
    private final RejectBrandEarlyUseCase rejectBrandEarlyUseCase;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('brand:approve', 'user:read')")
    public ResponseEntity<ApiResponse<List<BrandProfileView>>> list(
            @RequestParam(required = false) String status
    ) {
        List<String> statuses = status == null || status.isBlank()
                ? DEFAULT_APPROVAL_STATUSES
                : Arrays.stream(status.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(String::toUpperCase)
                        .toList();

        List<BrandProfileView> views = listBrandProfilesForAdminUseCase.execute(statuses);
        return ResponseEntity.ok(ApiResponse.<List<BrandProfileView>>builder()
                .message("Brand profiles retrieved successfully")
                .result(views)
                .build());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('brand:approve')")
    public ResponseEntity<ApiResponse<List<BrandProfileView>>> listPending() {
        List<BrandProfileView> views = listBrandProfilesForAdminUseCase.execute(
                List.of("PENDING_APPROVAL", "UNDER_REVIEW", "NEEDS_REVISION", "READY_FOR_FINAL_REVIEW")
        );
        return ResponseEntity.ok(ApiResponse.<List<BrandProfileView>>builder()
                .message("Pending brand profiles retrieved successfully")
                .result(views)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('brand:approve', 'user:read')")
    public ResponseEntity<ApiResponse<AdminBrandProfileDetailView>> getById(@PathVariable UUID id) {
        AdminBrandProfileDetailView view = getAdminBrandProfileDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<AdminBrandProfileDetailView>builder()
                .message("Brand profile detail retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("/{id}/reject-early")
    @PreAuthorize("hasAuthority('brand:approve')")
    public ResponseEntity<ApiResponse<BrandProfileView>> rejectEarly(
            @PathVariable UUID id,
            @RequestBody @Valid RejectBrandEarlyRequest request
    ) {
        BrandProfileView view = rejectBrandEarlyUseCase.execute(
                id,
                currentUserProvider.getCurrentUser().getUserId(),
                request.rejectionReason()
        );
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile rejected early successfully")
                .result(view)
                .build());
    }
}
