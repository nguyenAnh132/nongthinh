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
import com.nongthinh.bo_portal_service.application.view.UploadPolicyView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;
import com.nongthinh.bo_portal_service.presentation.mapper.FileConfigurationMapper;

@RestController
@RequestMapping("upload-policies")
@RequiredArgsConstructor
public class UploadPolicyController {
    private final GetFileUploadPolicyUseCase getFileUploadPolicyUseCase;
    private final FileConfigurationMapper mapper;

    @GetMapping("{purpose}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadPolicyView>> getPolicy(@PathVariable String purpose) {
        return ResponseEntity.ok(ApiResponse.<UploadPolicyView>builder()
                .message("Upload policy retrieved successfully")
                .result(Optional.of(mapper.toUploadPolicyView(getFileUploadPolicyUseCase.execute(purpose))))
                .build());
    }
}
