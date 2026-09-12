package com.nongthinh.rice_disease_diagnosis_service.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelRuntimeWarmupCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelruntime.WarmModelRuntimeUseCase;
import com.nongthinh.rice_disease_diagnosis_service.common.response.ApiResponse;
import com.nongthinh.rice_disease_diagnosis_service.configuration.InternalApiKeyVerifier;
import com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request.ModelRuntimeWarmupRequest;
import com.nongthinh.rice_disease_diagnosis_service.presentation.dto.response.ModelRuntimeWarmupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/model-runtimes")
@RequiredArgsConstructor
public class InternalModelRuntimeController {

    private final InternalApiKeyVerifier internalApiKeyVerifier;
    private final WarmModelRuntimeUseCase warmModelRuntimeUseCase;

    @PostMapping("/warm")
    public ResponseEntity<ApiResponse<ModelRuntimeWarmupResponse>> warm(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody @Valid ModelRuntimeWarmupRequest request) {
        internalApiKeyVerifier.verify(apiKey);
        boolean ready = warmModelRuntimeUseCase.execute(new ModelRuntimeWarmupCommand(
                request.modelVersionId(),
                request.artifactFileId(),
                request.artifactSha256(),
                request.inputWidth(),
                request.inputHeight()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                "Model runtime warm-up completed",
                new ModelRuntimeWarmupResponse(ready)
        ));
    }
}
