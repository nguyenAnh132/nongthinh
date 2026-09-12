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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.CreateAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.DeleteAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.GetAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.ListAiModelDiseaseMappingsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.UpdateAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelDiseaseMappingRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.AiModelDiseaseMappingMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("ai-model-disease-mappings")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AiModelDiseaseMappingController {

    private final ListAiModelDiseaseMappingsUseCase listUseCase;
    private final GetAiModelDiseaseMappingUseCase getUseCase;
    private final CreateAiModelDiseaseMappingUseCase createUseCase;
    private final UpdateAiModelDiseaseMappingUseCase updateUseCase;
    private final DeleteAiModelDiseaseMappingUseCase deleteUseCase;
    private final AiModelDiseaseMappingMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiModelDiseaseMappingView>>> list(
            @RequestParam UUID modelVersionId) {
        return ResponseEntity.ok(ApiResponse.<List<AiModelDiseaseMappingView>>builder()
                .message("AI model disease mappings retrieved successfully")
                .result(listUseCase.execute(modelVersionId))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiModelDiseaseMappingView>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<AiModelDiseaseMappingView>builder()
                .message("AI model disease mapping retrieved successfully")
                .result(getUseCase.execute(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiModelDiseaseMappingView>> create(
            @RequestBody @Valid AiModelDiseaseMappingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AiModelDiseaseMappingView>builder()
                        .message("AI model disease mapping created successfully")
                        .result(createUseCase.execute(mapper.toCommand(request)))
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiModelDiseaseMappingView>> update(
            @PathVariable UUID id,
            @RequestBody @Valid AiModelDiseaseMappingRequest request) {
        return ResponseEntity.ok(ApiResponse.<AiModelDiseaseMappingView>builder()
                .message("AI model disease mapping updated successfully")
                .result(updateUseCase.execute(id, mapper.toCommand(request)))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
