package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.ResolveAiModelDiseaseMappingsUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingResolutionView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelDiseaseMappingResolutionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/ai-model-mappings")
@PreAuthorize("hasAuthority('ROLE_INTERNAL')")
@RequiredArgsConstructor
public class InternalAiModelDiseaseMappingController {

    private final ResolveAiModelDiseaseMappingsUseCase resolveUseCase;

    @PostMapping("/resolve")
    public ResponseEntity<ApiResponse<List<AiModelDiseaseMappingResolutionView>>> resolve(
            @RequestBody @Valid AiModelDiseaseMappingResolutionRequest request) {
        return ResponseEntity.ok(ApiResponse.<List<AiModelDiseaseMappingResolutionView>>builder()
                .message("AI model disease mappings resolved successfully")
                .result(resolveUseCase.execute(
                        request.modelVersionId(), request.cropTypeId(), request.classCodes()))
                .build());
    }
}
