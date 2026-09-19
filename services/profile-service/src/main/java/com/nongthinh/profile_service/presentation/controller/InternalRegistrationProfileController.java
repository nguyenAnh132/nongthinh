package com.nongthinh.profile_service.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.profile_service.application.port.in.registration.CompleteRegistrationProfileUseCase;
import com.nongthinh.profile_service.presentation.dto.request.internal.CompleteRegistrationProfileRequest;
import com.nongthinh.profile_service.presentation.mapper.RegistrationProfileMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternalRegistrationProfileController {
    private final CompleteRegistrationProfileUseCase completeProfile;
    private final RegistrationProfileMapper mapper;

    @PostMapping("/internal/registration-profiles")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<Void> complete(@RequestBody @Valid CompleteRegistrationProfileRequest request) {
        completeProfile.execute(mapper.toCommand(request));
        return ResponseEntity.noContent().build();
    }
}
