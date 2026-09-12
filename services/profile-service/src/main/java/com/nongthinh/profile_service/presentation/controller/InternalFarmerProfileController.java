package com.nongthinh.profile_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.farmer.GetFarmerProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("internal/farmer-profiles")
@RequiredArgsConstructor
public class InternalFarmerProfileController {

    private final GetFarmerProfileByUserIdUseCase getFarmerProfileByUserIdUseCase;

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<FarmerProfileView>> getFarmerProfileByUserId(@PathVariable UUID userId) {
        FarmerProfileView view = getFarmerProfileByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.<FarmerProfileView>builder()
                .message("Farmer profile retrieved successfully")
                .result(view)
                .build());
    }

}
