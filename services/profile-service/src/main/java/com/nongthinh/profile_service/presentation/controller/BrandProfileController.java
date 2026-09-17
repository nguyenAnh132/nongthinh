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
import com.nongthinh.profile_service.application.port.in.brand.CreateMyBrandProfileUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandDocumentUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandProfileUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetPublicBrandProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.in.brand.GetPublicBrandProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandBannerUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandLogoUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandOfficeAddressUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandPhoneUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandProfileUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandRepresentativeEmailUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandRepresentativePhoneUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandWebsiteUseCase;
import com.nongthinh.profile_service.application.port.in.brand.SubmitMyBrandDocumentsUseCase;
import com.nongthinh.profile_service.application.port.in.brand.UploadMyBrandDocumentUseCase;
import com.nongthinh.profile_service.application.view.BrandDocumentView;
import com.nongthinh.profile_service.application.view.BrandProfilePublicView;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.me.UploadBrandDocumentRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.AddressUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.EmailUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.PhoneUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.UrlUpdateRequest;
import com.nongthinh.profile_service.presentation.mapper.BrandProfileMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("brand-profiles")
@RequiredArgsConstructor
public class BrandProfileController {

    private final GetMyBrandProfileUseCase getMyBrandProfileUseCase;
    private final CreateMyBrandProfileUseCase createMyBrandProfileUseCase;
    private final UpdateMyBrandProfileUseCase updateMyBrandProfileUseCase;
    private final UpdateMyBrandPhoneUseCase updateMyBrandPhoneUseCase;
    private final UpdateMyBrandLogoUseCase updateMyBrandLogoUseCase;
    private final UpdateMyBrandBannerUseCase updateMyBrandBannerUseCase;
    private final UpdateMyBrandWebsiteUseCase updateMyBrandWebsiteUseCase;
    private final UpdateMyBrandOfficeAddressUseCase updateMyBrandOfficeAddressUseCase;
    private final UpdateMyBrandRepresentativePhoneUseCase updateMyBrandRepresentativePhoneUseCase;
    private final UpdateMyBrandRepresentativeEmailUseCase updateMyBrandRepresentativeEmailUseCase;
    private final GetMyBrandDocumentUseCase getMyBrandDocumentUseCase;
    private final UploadMyBrandDocumentUseCase uploadMyBrandDocumentUseCase;
    private final SubmitMyBrandDocumentsUseCase submitMyBrandDocumentsUseCase;
    private final GetPublicBrandProfileByIdUseCase getPublicBrandProfileByIdUseCase;
    private final GetPublicBrandProfileByUserIdUseCase getPublicBrandProfileByUserIdUseCase;
    private final BrandProfileMapper brandProfileMapper;

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> getMyProfile() {
        BrandProfileView view = getMyBrandProfileUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<Void>> createMyProfile(
            @RequestBody @Valid MyBrandProfileCreationRequest request
    ) {
        createMyBrandProfileUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Void>builder()
                .message("Brand profile created successfully")
                .build());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyProfile(
            @RequestBody @Valid MyBrandProfileUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandProfileUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand profile updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/phone")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyPhone(
            @RequestBody @Valid PhoneUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandPhoneUseCase.execute(request.phone());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand phone updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/logo")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyLogo(
            @RequestBody @Valid UrlUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandLogoUseCase.execute(request.url());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand logo updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/banner")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyBanner(
            @RequestBody @Valid UrlUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandBannerUseCase.execute(request.url());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand banner updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/website")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyWebsite(
            @RequestBody @Valid UrlUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandWebsiteUseCase.execute(request.url());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand website updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/office-address")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyOfficeAddress(
            @RequestBody @Valid AddressUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandOfficeAddressUseCase.execute(
                brandProfileMapper.toUpdateMyBrandOfficeAddressCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand office address updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/representative-phone")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyRepresentativePhone(
            @RequestBody @Valid PhoneUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandRepresentativePhoneUseCase.execute(request.phone());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand representative phone updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/representative-email")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandProfileView>> updateMyRepresentativeEmail(
            @RequestBody @Valid EmailUpdateRequest request
    ) {
        BrandProfileView view = updateMyBrandRepresentativeEmailUseCase.execute(request.email());
        return ResponseEntity.ok(ApiResponse.<BrandProfileView>builder()
                .message("Brand representative email updated successfully")
                .result(view)
                .build());
    }

    @GetMapping("/me/documents")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandDocumentView>> getMyDocument() {
        BrandDocumentView view = getMyBrandDocumentUseCase.execute().orElse(null);
        return ResponseEntity.ok(ApiResponse.<BrandDocumentView>builder()
                .message("Brand document retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("/me/documents")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<BrandDocumentView>> uploadMyDocument(
            @RequestBody @Valid UploadBrandDocumentRequest request
    ) {
        BrandDocumentView view = uploadMyBrandDocumentUseCase.execute(request.businessLicenseUrl());
        return ResponseEntity.ok(ApiResponse.<BrandDocumentView>builder()
                .message("Brand document uploaded successfully")
                .result(view)
                .build());
    }

    @PostMapping("/me/documents-submitted")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ResponseEntity<ApiResponse<Void>> submitMyDocuments() {
        submitMyBrandDocumentsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Brand documents submitted successfully")
                .build());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<BrandProfilePublicView>> getPublicProfileByUserId(@PathVariable UUID userId) {
        BrandProfilePublicView view = getPublicBrandProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<BrandProfilePublicView>builder()
                .message("Brand profile retrieved successfully")
                .result(view)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandProfilePublicView>> getPublicProfile(@PathVariable UUID id) {
        BrandProfilePublicView view = getPublicBrandProfileByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<BrandProfilePublicView>builder()
                .message("Brand profile retrieved successfully")
                .result(view)
                .build());
    }
}
