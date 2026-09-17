package com.nongthinh.rice_disease_diagnosis_service.presentation.controller;

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
import com.nongthinh.rice_disease_diagnosis_service.application.command.DiagnosisCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.CreateDiagnosisUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.GetDiagnosisHistoryUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.ListDiagnosisHistoriesUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryDetailView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryListItemView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisResponseView;
import com.nongthinh.rice_disease_diagnosis_service.common.response.ApiResponse;
import com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request.DiagnosisRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("diagnoses")
@PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
@RequiredArgsConstructor
public class DiagnosisController {
    private final CreateDiagnosisUseCase createDiagnosisUseCase;
    private final ListDiagnosisHistoriesUseCase listDiagnosisHistoriesUseCase;
    private final GetDiagnosisHistoryUseCase getDiagnosisHistoryUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisResponseView>> diagnose(@RequestBody @Valid DiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Diagnosis completed successfully",
                createDiagnosisUseCase.execute(new DiagnosisCommand(request.cropTypeId(), request.fileIds()))));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<DiagnosisHistoryListItemView>>> history() {
        return ResponseEntity.ok(ApiResponse.success(
                "Diagnosis histories retrieved successfully", listDiagnosisHistoriesUseCase.execute()));
    }

    @GetMapping("/history/{diagnosisId}")
    public ResponseEntity<ApiResponse<DiagnosisHistoryDetailView>> historyDetail(@PathVariable UUID diagnosisId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Diagnosis history retrieved successfully", getDiagnosisHistoryUseCase.execute(diagnosisId)));
    }
}
