package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.CreateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.DeleteAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.GetAiModelByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.ListAiModelsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.UpdateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.AiModelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("ai-models")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AiModelController {

    private final ListAiModelsUseCase listAiModelsUseCase;
    private final GetAiModelByIdUseCase getAiModelByIdUseCase;
    private final CreateAiModelUseCase createAiModelUseCase;
    private final UpdateAiModelUseCase updateAiModelUseCase;
    private final DeleteAiModelUseCase deleteAiModelUseCase;
    private final AiModelMapper aiModelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiModelView>>> listAiModels() {
        return ResponseEntity.ok(ApiResponse.<List<AiModelView>>builder()
                .message("AI models retrieved successfully")
                .result(listAiModelsUseCase.execute())
                .build());
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<ApiResponse<AiModelView>> getAiModel(@PathVariable UUID modelId) {
        return ResponseEntity.ok(ApiResponse.<AiModelView>builder()
                .message("AI model retrieved successfully")
                .result(getAiModelByIdUseCase.execute(modelId))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiModelView>> createAiModel(
            @RequestBody @Valid AiModelCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AiModelView>builder()
                        .message("AI model created successfully")
                        .result(createAiModelUseCase.execute(
                                aiModelMapper.toAiModelCreationCommand(request)
                        ))
                        .build());
    }

    @PutMapping("/{modelId}")
    public ResponseEntity<ApiResponse<AiModelView>> updateAiModel(
            @PathVariable UUID modelId,
            @RequestBody @Valid AiModelUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<AiModelView>builder()
                .message("AI model updated successfully")
                .result(updateAiModelUseCase.execute(
                        modelId,
                        aiModelMapper.toAiModelUpdateCommand(request)
                ))
                .build());
    }

    @DeleteMapping("/{modelId}")
    public ResponseEntity<ApiResponse<Void>> retireAiModel(@PathVariable UUID modelId) {
        deleteAiModelUseCase.execute(modelId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("AI model retired successfully")
                .build());
    }
}
