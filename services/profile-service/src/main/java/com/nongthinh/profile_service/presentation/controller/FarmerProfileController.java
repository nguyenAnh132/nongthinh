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
import com.nongthinh.profile_service.application.port.in.farmer.CreateMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.GetMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.GetPublicFarmerProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.GetPublicFarmerProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerAddressUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerAvatarUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerPhoneUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.view.FarmerProfilePublicView;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.AddressUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.PhoneUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.UrlUpdateRequest;
import com.nongthinh.profile_service.presentation.mapper.FarmerProfileMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("farmer-profiles")
@RequiredArgsConstructor
public class FarmerProfileController {

    private final GetMyFarmerProfileUseCase getMyFarmerProfileUseCase;
    private final CreateMyFarmerProfileUseCase createMyFarmerProfileUseCase;
    private final UpdateMyFarmerProfileUseCase updateMyFarmerProfileUseCase;
    private final UpdateMyFarmerPhoneUseCase updateMyFarmerPhoneUseCase;
    private final UpdateMyFarmerAvatarUseCase updateMyFarmerAvatarUseCase;
    private final UpdateMyFarmerAddressUseCase updateMyFarmerAddressUseCase;
    private final GetPublicFarmerProfileByIdUseCase getPublicFarmerProfileByIdUseCase;
    private final GetPublicFarmerProfileByUserIdUseCase getPublicFarmerProfileByUserIdUseCase;
    private final FarmerProfileMapper farmerProfileMapper;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> getMyProfile() {
        FarmerProfileView view = getMyFarmerProfileUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer profile retrieved successfully")
                .result(view)
                .build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<Void>> createMyProfile(
            @RequestBody @Valid MyFarmerProfileCreationRequest request
    ) {
        createMyFarmerProfileUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Void>builder()
                .message("Farmer profile created successfully")
                .build());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> updateMyProfile(
            @RequestBody @Valid MyFarmerProfileUpdateRequest request
    ) {
        FarmerProfileView view = updateMyFarmerProfileUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer profile updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/phone")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> updateMyPhone(
            @RequestBody @Valid PhoneUpdateRequest request
    ) {
        FarmerProfileView view = updateMyFarmerPhoneUseCase.execute(request.phone());
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer phone updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/avatar")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> updateMyAvatar(
            @RequestBody @Valid UrlUpdateRequest request
    ) {
        FarmerProfileView view = updateMyFarmerAvatarUseCase.execute(request.url());
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer avatar updated successfully")
                .result(view)
                .build());
    }

    @PatchMapping("/me/address")
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> updateMyAddress(
            @RequestBody @Valid AddressUpdateRequest request
    ) {
        FarmerProfileView view = updateMyFarmerAddressUseCase.execute(
                farmerProfileMapper.toUpdateMyFarmerAddressCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer address updated successfully")
                .result(view)
                .build());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<FarmerProfilePublicView>> getPublicProfileByUserId(@PathVariable UUID userId) {
        FarmerProfilePublicView view = getPublicFarmerProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<FarmerProfilePublicView>builder()
                .message("Farmer profile retrieved successfully")
                .result(view)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmerProfilePublicView>> getPublicProfile(@PathVariable UUID id) {
        FarmerProfilePublicView view = getPublicFarmerProfileByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<FarmerProfilePublicView>builder()
                .message("Farmer profile retrieved successfully")
                .result(view)
                .build());
    }
}
