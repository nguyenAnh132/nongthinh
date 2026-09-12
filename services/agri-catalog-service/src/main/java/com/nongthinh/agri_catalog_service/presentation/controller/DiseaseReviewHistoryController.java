package com.nongthinh.agri_catalog_service.presentation.controller;

import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListBrandDiseaseReviewHistoriesUseCase;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("disease-review-histories")
@RequiredArgsConstructor
public class DiseaseReviewHistoryController {

    private final ListBrandDiseaseReviewHistoriesUseCase listHistoriesUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageView<DiseaseReviewHistoryListItemView>>> list(
            @RequestParam(required = false) String createdSource,
            @RequestParam(required = false) UUID diseaseId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) DiseaseReviewAction action,
            @RequestParam(required = false) ReviewStatus newStatus,
            @RequestParam(required = false) ReviewActorType actorType,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageView<DiseaseReviewHistoryListItemView> result =
                listHistoriesUseCase.execute(new DiseaseReviewHistoryQuery(
                        parseCreatedSource(createdSource),
                        diseaseId,
                        brandId,
                        action,
                        newStatus,
                        actorType,
                        actorId,
                        keyword,
                        from,
                        to,
                        page,
                        size
                ));
        return ResponseEntity.ok(
                ApiResponse.<PageView<DiseaseReviewHistoryListItemView>>builder()
                        .code("1000")
                        .message("Disease review histories retrieved successfully")
                        .result(result)
                        .build()
        );
    }

    private static CreatedSource parseCreatedSource(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("BRAND".equals(value.trim())) {
            return CreatedSource.BRAND;
        }
        throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
    }
}
