package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.command.admin.RecordBrandVerificationLogCommand;
import com.nongthinh.profile_service.application.port.in.admin.RecordBrandVerificationLogUseCase;
import com.nongthinh.profile_service.application.view.BrandVerificationLogView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.admin.CreateBrandVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/brand-profiles")
@RequiredArgsConstructor
public class AdminBrandVerificationController {

    private final RecordBrandVerificationLogUseCase recordBrandVerificationLogUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/{id}/verifications")
    @PreAuthorize("hasAuthority('brand:verify')")
    public ResponseEntity<ApiResponse<BrandVerificationLogView>> create(
            @PathVariable UUID id,
            @RequestBody @Valid CreateBrandVerificationRequest request
    ) {
        BrandVerificationLogView view = recordBrandVerificationLogUseCase.execute(
                new RecordBrandVerificationLogCommand(
                        id,
                        currentUserProvider.getCurrentUser().getUserId(),
                        request.phoneCalled(),
                        request.result(),
                        request.note()
                )
        );
        return ResponseEntity.ok(ApiResponse.<BrandVerificationLogView>builder()
                .message("Brand verification recorded successfully")
                .result(view)
                .build());
    }
}
