package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.ActivateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.CreateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.DeactivateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.GetAiModelDeploymentsUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelDeploymentCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.AiModelDeploymentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("ai-model-deployments")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AiModelDeploymentController {

    private final GetAiModelDeploymentsUseCase getAiModelDeploymentsUseCase;
    private final CreateAiModelDeploymentUseCase createAiModelDeploymentUseCase;
    private final ActivateAiModelDeploymentUseCase activateAiModelDeploymentUseCase;
    private final DeactivateAiModelDeploymentUseCase deactivateAiModelDeploymentUseCase;
    private final AiModelDeploymentMapper aiModelDeploymentMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiModelDeploymentView>>> list() {
        return ResponseEntity.ok(ApiResponse.<List<AiModelDeploymentView>>builder()
                .message("AI model deployments retrieved successfully")
                .result(getAiModelDeploymentsUseCase.execute())
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiModelDeploymentView>> create(
            @RequestBody @Valid AiModelDeploymentCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AiModelDeploymentView>builder()
                        .message("AI model deployment created successfully")
                        .result(createAiModelDeploymentUseCase.execute(aiModelDeploymentMapper.toCommand(request)))
                        .build());
    }

    @PostMapping("/{deploymentId}/activate")
    public ResponseEntity<ApiResponse<AiModelDeploymentView>> activate(@PathVariable UUID deploymentId) {
        return ResponseEntity.ok(ApiResponse.<AiModelDeploymentView>builder()
                .message("AI model deployment activated successfully")
                .result(activateAiModelDeploymentUseCase.execute(deploymentId))
                .build());
    }

    @PostMapping("/{deploymentId}/deactivate")
    public ResponseEntity<ApiResponse<AiModelDeploymentView>> deactivate(@PathVariable UUID deploymentId) {
        return ResponseEntity.ok(ApiResponse.<AiModelDeploymentView>builder()
                .message("AI model deployment deactivated successfully")
                .result(deactivateAiModelDeploymentUseCase.execute(deploymentId))
                .build());
    }
}
