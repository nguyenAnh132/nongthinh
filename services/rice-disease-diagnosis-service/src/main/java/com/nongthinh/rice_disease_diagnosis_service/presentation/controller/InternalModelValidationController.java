package com.nongthinh.rice_disease_diagnosis_service.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelArtifactValidationCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelClassManifestItem;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelvalidation.ValidateModelArtifactUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelArtifactValidationResult;
import com.nongthinh.rice_disease_diagnosis_service.common.response.ApiResponse;
import com.nongthinh.rice_disease_diagnosis_service.configuration.InternalApiKeyVerifier;
import com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request.ModelArtifactValidationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/model-validations")
@RequiredArgsConstructor
public class InternalModelValidationController {

    private final InternalApiKeyVerifier internalApiKeyVerifier;
    private final ValidateModelArtifactUseCase validateModelArtifactUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<ModelArtifactValidationResult>> validate(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody @Valid ModelArtifactValidationRequest request) {
        internalApiKeyVerifier.verify(apiKey);
        ModelArtifactValidationResult result = validateModelArtifactUseCase.execute(
                new ModelArtifactValidationCommand(
                        request.modelVersionId(),
                        request.artifactFileId(),
                        request.artifactSha256(),
                        request.inputWidth(),
                        request.inputHeight(),
                        request.classes().stream()
                                .map(item -> new ModelClassManifestItem(
                                        item.classIndex(), item.classCode(), item.classKind()))
                                .toList()
                )
        );
        return ResponseEntity.ok(ApiResponse.success("Model artifact validation completed", result));
    }
}
