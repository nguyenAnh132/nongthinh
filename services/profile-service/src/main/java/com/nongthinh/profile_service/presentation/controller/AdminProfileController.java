package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.admin.CreateMyAdminProfileUseCase;
import com.nongthinh.profile_service.application.port.in.admin.GetMyAdminProfileUseCase;
import com.nongthinh.profile_service.application.port.in.admin.GetPublicAdminProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.in.admin.GetPublicAdminProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateMyAdminAvatarUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateMyAdminPhoneUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateMyAdminProfileUseCase;
import com.nongthinh.profile_service.application.view.AdminProfilePublicView;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.PhoneUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.UrlUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin-profiles")
@RequiredArgsConstructor
public class AdminProfileController {

    private final GetMyAdminProfileUseCase getMyAdminProfileUseCase;
    private final CreateMyAdminProfileUseCase createMyAdminProfileUseCase;
    private final UpdateMyAdminProfileUseCase updateMyAdminProfileUseCase;
    private final UpdateMyAdminPhoneUseCase updateMyAdminPhoneUseCase;
    private final UpdateMyAdminAvatarUseCase updateMyAdminAvatarUseCase;
    private final GetPublicAdminProfileByIdUseCase getPublicAdminProfileByIdUseCase;
    private final GetPublicAdminProfileByUserIdUseCase getPublicAdminProfileByUserIdUseCase;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfileView>> getMyProfile() {
        AdminProfileView view = getMyAdminProfileUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin profile retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createMyProfile(
            @RequestBody @Valid MyAdminProfileCreationRequest request
    ) {
        createMyAdminProfileUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Void>builder()
                .message("Admin profile created successfully")
                .build());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfileView>> updateMyProfile(
            @RequestBody @Valid MyAdminProfileUpdateRequest request
    ) {
        AdminProfileView view = updateMyAdminProfileUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin profile updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/phone")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfileView>> updateMyPhone(
            @RequestBody @Valid PhoneUpdateRequest request
    ) {
        AdminProfileView view = updateMyAdminPhoneUseCase.execute(request.phone());
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin phone updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/avatar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfileView>> updateMyAvatar(
            @RequestBody @Valid UrlUpdateRequest request
    ) {
        AdminProfileView view = updateMyAdminAvatarUseCase.execute(request.url());
        return ResponseEntity.ok(ApiResponse.<AdminProfileView>builder()
                .message("Admin avatar updated successfully")
                .result(view)
                .build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfilePublicView>> getPublicProfileByUserId(@PathVariable UUID userId) {
        AdminProfilePublicView view = getPublicAdminProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<AdminProfilePublicView>builder()
                .message("Admin profile retrieved successfully")
                .result(view)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProfilePublicView>> getPublicProfile(@PathVariable UUID id) {
        AdminProfilePublicView view = getPublicAdminProfileByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<AdminProfilePublicView>builder()
                .message("Admin profile retrieved successfully")
                .result(view)
                .build());
    }
}
