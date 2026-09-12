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
import com.nongthinh.agri_catalog_service.application.port.in.disease.ApproveDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.CreateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.DeleteDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.HideDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseaseReviewHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseasesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.RejectDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.RestoreDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.SubmitDiseaseForReviewUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.UpdateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryView;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.presentation.dto.request.DiseaseCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.DiseaseRejectionRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.DiseaseUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.DiseaseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final ListDiseasesUseCase listDiseasesUseCase;
    private final GetDiseaseByIdUseCase getDiseaseByIdUseCase;
    private final CreateDiseaseUseCase createDiseaseUseCase;
    private final UpdateDiseaseUseCase updateDiseaseUseCase;
    private final DeleteDiseaseUseCase deleteDiseaseUseCase;
    private final SubmitDiseaseForReviewUseCase submitDiseaseForReviewUseCase;
    private final ApproveDiseaseUseCase approveDiseaseUseCase;
    private final RejectDiseaseUseCase rejectDiseaseUseCase;
    private final HideDiseaseUseCase hideDiseaseUseCase;
    private final RestoreDiseaseUseCase restoreDiseaseUseCase;
    private final ListDiseaseReviewHistoryUseCase listDiseaseReviewHistoryUseCase;
    private final DiseaseMapper diseaseMapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<List<DiseaseView>>> getDiseases(
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) UUID cropTypeId
    ) {
        List<DiseaseView> result = listDiseasesUseCase.execute(
                brandId,
                reviewStatus,
                cropTypeId,
                false
        );
        return ResponseEntity.ok(ApiResponse.<List<DiseaseView>>builder()
                .message("Diseases retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<DiseaseView>> getDiseaseById(
            @PathVariable UUID id
    ) {
        DiseaseView result = getDiseaseByIdUseCase.execute(id, false);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<DiseaseView>> createDisease(
            @RequestBody @Valid DiseaseCreationRequest request
    ) {
        DiseaseView result = createDiseaseUseCase.execute(
                diseaseMapper.toDiseaseCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DiseaseView>builder()
                        .message("Disease created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<DiseaseView>> updateDisease(
            @PathVariable UUID id,
            @RequestBody @Valid DiseaseUpdateRequest request
    ) {
        DiseaseView result = updateDiseaseUseCase.execute(
                id,
                diseaseMapper.toDiseaseUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease updated successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<Void>> deleteDisease(@PathVariable UUID id) {
        deleteDiseaseUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Disease deleted successfully")
                .build());
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('ROLE_BRAND')")
    public ResponseEntity<ApiResponse<DiseaseView>> submitDisease(
            @PathVariable UUID id
    ) {
        DiseaseView result = submitDiseaseForReviewUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease submitted for review successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DiseaseView>> approveDisease(
            @PathVariable UUID id
    ) {
        DiseaseView result = approveDiseaseUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease approved successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DiseaseView>> rejectDisease(
            @PathVariable UUID id,
            @RequestBody @Valid DiseaseRejectionRequest request
    ) {
        DiseaseView result = rejectDiseaseUseCase.execute(id, request.reason());
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease rejected successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/hide")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DiseaseView>> hideDisease(
            @PathVariable UUID id
    ) {
        DiseaseView result = hideDiseaseUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease hidden successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DiseaseView>> restoreDisease(
            @PathVariable UUID id
    ) {
        DiseaseView result = restoreDiseaseUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease restored successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}/review-history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<List<DiseaseReviewHistoryView>>> getReviewHistory(
            @PathVariable UUID id
    ) {
        List<DiseaseReviewHistoryView> result =
                listDiseaseReviewHistoryUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<List<DiseaseReviewHistoryView>>builder()
                .message("Disease review history retrieved successfully")
                .result(result)
                .build());
    }
}
