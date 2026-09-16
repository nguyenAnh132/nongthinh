package com.nongthinh.bo_portal_service.presentation.controller;

import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.GetFileUploadPolicyUseCase;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.ListFileTypesUseCase;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.UpdateFilePurposeTypeUseCase;
import com.nongthinh.bo_portal_service.application.view.FileTypeView;
import com.nongthinh.bo_portal_service.application.view.FilePurposeTypeView;
import com.nongthinh.bo_portal_service.application.view.FileUploadPolicyView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateFilePurposeTypeRequest;
import com.nongthinh.bo_portal_service.presentation.mapper.FileConfigurationMapper;

@RestController
@RequiredArgsConstructor
public class FileConfigurationController {
    private final ListFileTypesUseCase listFileTypesUseCase;
    private final GetFileUploadPolicyUseCase getFileUploadPolicyUseCase;
    private final UpdateFilePurposeTypeUseCase updateFilePurposeTypeUseCase;
    private final FileConfigurationMapper mapper;

    @GetMapping("file-types")
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<List<FileTypeView>>> listFileTypes() {
        return ResponseEntity.ok(ApiResponse.<List<FileTypeView>>builder()
                .message("File types retrieved successfully")
                .result(Optional.of(listFileTypesUseCase.execute())).build());
    }

    @GetMapping("file-upload-policies/{purpose}")
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<FileUploadPolicyView>> getPolicy(@PathVariable String purpose) {
        return ResponseEntity.ok(ApiResponse.<FileUploadPolicyView>builder()
                .message("File upload policy retrieved successfully")
                .result(Optional.of(getFileUploadPolicyUseCase.execute(purpose))).build());
    }

    @PutMapping("file-upload-policies/{purpose}/file-types/{code}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<FilePurposeTypeView>> updateFileType(
            @PathVariable String purpose, @PathVariable String code,
            @RequestBody @Valid UpdateFilePurposeTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.<FilePurposeTypeView>builder()
                .message("File purpose type updated successfully")
                .result(Optional.of(updateFilePurposeTypeUseCase.execute(purpose, code,
                        mapper.toUpdateCommand(request)))).build());
    }
}
