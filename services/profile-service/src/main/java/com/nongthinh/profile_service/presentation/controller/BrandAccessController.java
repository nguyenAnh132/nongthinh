package com.nongthinh.profile_service.presentation.controller;

import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandAccessUseCase;
import com.nongthinh.profile_service.application.view.BrandAccessView;
import com.nongthinh.profile_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BrandAccessController {
    private final GetMyBrandAccessUseCase getMyBrandAccessUseCase;

    @GetMapping("/brand-profiles/me/access")
    @PreAuthorize("hasAnyAuthority('ROLE_BRAND', 'ROLE_BRAND_PENDING')")
    public ApiResponse<BrandAccessView> getMyAccess() {
        return ApiResponse.<BrandAccessView>builder()
                .result(getMyBrandAccessUseCase.execute()).build();
    }
}
