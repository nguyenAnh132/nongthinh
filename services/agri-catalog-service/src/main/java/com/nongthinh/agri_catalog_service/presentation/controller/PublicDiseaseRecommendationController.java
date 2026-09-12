package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseRecommendationsUseCase;
import com.nongthinh.agri_catalog_service.application.view.DiseaseRecommendationView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequestMapping("public/diseases/{diseaseId}/recommendations")
@RequiredArgsConstructor
public class PublicDiseaseRecommendationController {

    private final GetDiseaseRecommendationsUseCase recommendationsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiseaseRecommendationView>>> getRecommendations(
            @PathVariable UUID diseaseId,
            @RequestParam(defaultValue = "3") @Min(value = 1, message = "INVALID_REQUEST_PARAMETER") int limit) {
        return ResponseEntity.ok(ApiResponse.<List<DiseaseRecommendationView>>builder()
                .message("Disease recommendations retrieved successfully")
                .result(recommendationsUseCase.execute(diseaseId, limit))
                .build());
    }
}
