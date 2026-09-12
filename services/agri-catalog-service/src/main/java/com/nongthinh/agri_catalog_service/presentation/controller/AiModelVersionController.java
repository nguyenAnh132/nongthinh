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
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.CreateAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.GetAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.ListAiModelVersionsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.MarkAiModelVersionReadyUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.RetireAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.ValidateAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelVersionCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.AiModelVersionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AiModelVersionController {

    private final ListAiModelVersionsUseCase listAiModelVersionsUseCase;
    private final CreateAiModelVersionUseCase createAiModelVersionUseCase;
    private final GetAiModelVersionUseCase getAiModelVersionUseCase;
    private final ValidateAiModelVersionUseCase validateAiModelVersionUseCase;
    private final MarkAiModelVersionReadyUseCase markAiModelVersionReadyUseCase;
    private final RetireAiModelVersionUseCase retireAiModelVersionUseCase;
    private final AiModelVersionMapper aiModelVersionMapper;

    @GetMapping("/ai-models/{modelId}/versions")
    public ResponseEntity<ApiResponse<List<AiModelVersionView>>> list(@PathVariable UUID modelId) {
        return ResponseEntity.ok(ApiResponse.<List<AiModelVersionView>>builder()
                .message("AI model versions retrieved successfully")
                .result(listAiModelVersionsUseCase.execute(modelId))
                .build());
    }

    @PostMapping("/ai-models/{modelId}/versions")
    public ResponseEntity<ApiResponse<AiModelVersionView>> create(
            @PathVariable UUID modelId,
            @RequestBody @Valid AiModelVersionCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AiModelVersionView>builder()
                        .message("AI model version created successfully")
                        .result(createAiModelVersionUseCase.execute(modelId, aiModelVersionMapper.toCommand(request)))
                        .build());
    }

    @GetMapping("/ai-model-versions/{versionId}")
    public ResponseEntity<ApiResponse<AiModelVersionView>> get(@PathVariable UUID versionId) {
        return ResponseEntity.ok(ApiResponse.<AiModelVersionView>builder()
                .message("AI model version retrieved successfully")
                .result(getAiModelVersionUseCase.execute(versionId))
                .build());
    }

    @PostMapping("/ai-model-versions/{versionId}/validate")
    public ResponseEntity<ApiResponse<AiModelVersionView>> validate(@PathVariable UUID versionId) {
        return ResponseEntity.ok(ApiResponse.<AiModelVersionView>builder()
                .message("AI model version validation completed")
                .result(validateAiModelVersionUseCase.execute(versionId))
                .build());
    }

    @PostMapping("/ai-model-versions/{versionId}/ready")
    public ResponseEntity<ApiResponse<AiModelVersionView>> markReady(@PathVariable UUID versionId) {
        return ResponseEntity.ok(ApiResponse.<AiModelVersionView>builder()
                .message("AI model version is ready")
                .result(markAiModelVersionReadyUseCase.execute(versionId))
                .build());
    }

    @PostMapping("/ai-model-versions/{versionId}/retire")
    public ResponseEntity<ApiResponse<AiModelVersionView>> retire(@PathVariable UUID versionId) {
        return ResponseEntity.ok(ApiResponse.<AiModelVersionView>builder()
                .message("AI model version retired successfully")
                .result(retireAiModelVersionUseCase.execute(versionId))
                .build());
    }
}
