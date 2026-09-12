package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.command.admin.RecordBrandVerificationLogCommand;
import com.nongthinh.profile_service.application.command.admin.ReviewBrandDocumentCommand;
import com.nongthinh.profile_service.application.port.in.admin.RecordBrandVerificationLogUseCase;
import com.nongthinh.profile_service.application.port.in.admin.RecordBrandDocumentsRequestedUseCase;
import com.nongthinh.profile_service.application.port.in.admin.ReviewBrandDocumentUseCase;
import com.nongthinh.profile_service.application.port.in.admin.UpdateBrandProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetBrandProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetBrandProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.application.view.BrandVerificationLogView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.admin.BrandProfileStatusUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.internal.InternalCreateBrandVerificationRequest;
import com.nongthinh.profile_service.presentation.dto.request.internal.InternalRecordDocumentsRequestedRequest;
import com.nongthinh.profile_service.presentation.dto.request.internal.InternalReviewBrandDocumentRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("internal/brand-profiles")
@RequiredArgsConstructor
@Slf4j
public class InternalBrandProfileController {

    private final GetBrandProfileByUserIdUseCase getBrandProfileByUserIdUseCase;
    private final GetBrandProfileByIdUseCase getBrandProfileByIdUseCase;
    private final UpdateBrandProfileStatusUseCase updateBrandProfileStatusUseCase;
    private final RecordBrandVerificationLogUseCase recordBrandVerificationLogUseCase;
    private final RecordBrandDocumentsRequestedUseCase recordBrandDocumentsRequestedUseCase;
    private final ReviewBrandDocumentUseCase reviewBrandDocumentUseCase;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<BrandProfileView>> getBrandProfileById(@PathVariable UUID id) {
        BrandProfileView view = getBrandProfileByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile retrieved successfully")
                .result(view)
                .build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<BrandProfileView>> getBrandProfileByUserId(@PathVariable UUID userId) {
        log.info("Get brand profile");
        BrandProfileView view = getBrandProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile retrieved successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateBrandProfileStatus(
        @PathVariable UUID id, @RequestBody @Valid BrandProfileStatusUpdateRequest request
    ) {
        BrandProfileView view = updateBrandProfileStatusUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile status updated successfully")
                .result(view)
                .build());
    }

    @PostMapping("/{id}/verifications")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<BrandVerificationLogView>> createVerificationLog(
            @PathVariable UUID id,
            @RequestBody @Valid InternalCreateBrandVerificationRequest request
    ) {
        BrandVerificationLogView view = recordBrandVerificationLogUseCase.execute(
                new RecordBrandVerificationLogCommand(
                        id,
                        request.adminUserId(),
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

    @PostMapping("/{id}/documents-requested")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<Void>> recordDocumentsRequested(
            @PathVariable UUID id,
            @RequestBody @Valid InternalRecordDocumentsRequestedRequest request
    ) {
        recordBrandDocumentsRequestedUseCase.execute(id, request.actorUserId());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Brand documents request recorded successfully")
                .build());
    }

    @PostMapping("/{id}/documents/review")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<BrandProfileView>> reviewBrandDocument(
            @PathVariable UUID id,
            @RequestBody @Valid InternalReviewBrandDocumentRequest request
    ) {
        BrandProfileView view = reviewBrandDocumentUseCase.execute(
                new ReviewBrandDocumentCommand(
                        id,
                        request.adminUserId(),
                        request.documentsOk(),
                        request.revisionReason()
                )
        );
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand document reviewed successfully")
                .result(view)
                .build());
    }
}
