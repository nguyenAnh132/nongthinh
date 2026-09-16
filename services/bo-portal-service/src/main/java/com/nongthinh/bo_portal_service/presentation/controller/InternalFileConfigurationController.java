package com.nongthinh.bo_portal_service.presentation.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.GetFileUploadPolicyUseCase;
import com.nongthinh.bo_portal_service.application.view.FileUploadPolicyView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;

@RestController
@RequestMapping("internal/file-upload-policies")
@RequiredArgsConstructor
public class InternalFileConfigurationController {
    private final GetFileUploadPolicyUseCase getFileUploadPolicyUseCase;

    @GetMapping("{purpose}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<FileUploadPolicyView>> getPolicy(@PathVariable String purpose) {
        return ResponseEntity.ok(ApiResponse.<FileUploadPolicyView>builder()
                .message("File upload policy retrieved successfully")
                .result(Optional.of(getFileUploadPolicyUseCase.execute(purpose))).build());
    }
}
