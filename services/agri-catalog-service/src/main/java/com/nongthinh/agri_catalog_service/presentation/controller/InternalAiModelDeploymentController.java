package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.ResolveActiveAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentResolutionView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/ai-model-deployments")
@PreAuthorize("hasAuthority('ROLE_INTERNAL')")
@RequiredArgsConstructor
public class InternalAiModelDeploymentController {

    private final ResolveActiveAiModelDeploymentUseCase resolveActiveAiModelDeploymentUseCase;

    @GetMapping("/resolve")
    public ResponseEntity<ApiResponse<AiModelDeploymentResolutionView>> resolve(
            @RequestParam UUID cropTypeId) {
        return ResponseEntity.ok(ApiResponse.<AiModelDeploymentResolutionView>builder()
                .message("Active AI model deployment resolved successfully")
                .result(resolveActiveAiModelDeploymentUseCase.execute(cropTypeId))
                .build());
    }
}
