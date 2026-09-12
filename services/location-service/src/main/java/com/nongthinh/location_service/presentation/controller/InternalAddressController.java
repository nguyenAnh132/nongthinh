package com.nongthinh.location_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.location_service.application.port.in.address.ValidateAddressUseCase;
import com.nongthinh.location_service.application.port.in.address.ResolveAddressUseCase;
import com.nongthinh.location_service.application.view.AddressView;
import com.nongthinh.location_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("internal/addresses")
@RequiredArgsConstructor
public class InternalAddressController {

    private final ValidateAddressUseCase validateAddressUseCase;
    private final ResolveAddressUseCase resolveAddressUseCase;

    @GetMapping("/validate")
    @PreAuthorize("hasRole('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<Boolean>> validateAddress(
            @RequestParam(required = false) String provinceId,
            @RequestParam(required = false) UUID communeId
    ) {
        boolean valid = validateAddressUseCase.execute(provinceId, communeId);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .message(valid ? "Address is valid" : "Address is invalid")
                .result(Optional.of(valid))
                .build());
    }

    @GetMapping("/resolve")
    @PreAuthorize("hasRole('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<AddressView>> resolveAddress(
            @RequestParam String provinceId,
            @RequestParam UUID communeId
    ) {
        AddressView view = resolveAddressUseCase.execute(provinceId, communeId);
        return ResponseEntity.ok(ApiResponse.<AddressView>builder()
                .message("Address resolved successfully")
                .result(Optional.of(view))
                .build());
    }
}
