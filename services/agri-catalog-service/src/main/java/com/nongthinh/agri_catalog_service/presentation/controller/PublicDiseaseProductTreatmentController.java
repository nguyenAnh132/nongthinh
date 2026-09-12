package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.ListDiseaseProductTreatmentsUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/diseases/{diseaseId}/product-treatments")
@RequiredArgsConstructor
public class PublicDiseaseProductTreatmentController {

    private final ListDiseaseProductTreatmentsUseCase listTreatmentsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDiseaseTreatmentView>>> getTreatments(
            @PathVariable UUID diseaseId
    ) {
        List<ProductDiseaseTreatmentView> result =
                listTreatmentsUseCase.execute(diseaseId, true);
        return ResponseEntity.ok(ApiResponse.<List<ProductDiseaseTreatmentView>>builder()
                .message("Disease product treatments retrieved successfully")
                .result(result)
                .build());
    }
}
