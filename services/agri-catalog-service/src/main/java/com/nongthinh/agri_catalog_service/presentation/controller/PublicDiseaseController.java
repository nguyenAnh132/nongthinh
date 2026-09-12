package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseasesUseCase;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/diseases")
@RequiredArgsConstructor
public class PublicDiseaseController {

    private final ListDiseasesUseCase listDiseasesUseCase;
    private final GetDiseaseByIdUseCase getDiseaseByIdUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiseaseView>>> getPublishedDiseases(
            @RequestParam(required = false) UUID cropTypeId
    ) {
        List<DiseaseView> result = listDiseasesUseCase.execute(
                null,
                null,
                cropTypeId,
                true
        );
        return ResponseEntity.ok(ApiResponse.<List<DiseaseView>>builder()
                .message("Diseases retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiseaseView>> getPublishedDiseaseById(
            @PathVariable UUID id
    ) {
        DiseaseView result = getDiseaseByIdUseCase.execute(id, true);
        return ResponseEntity.ok(ApiResponse.<DiseaseView>builder()
                .message("Disease retrieved successfully")
                .result(result)
                .build());
    }
}
