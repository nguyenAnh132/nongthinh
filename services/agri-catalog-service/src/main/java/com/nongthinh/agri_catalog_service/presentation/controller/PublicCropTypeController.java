package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.ListCropTypesUseCase;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/crop-types")
@RequiredArgsConstructor
public class PublicCropTypeController {

    private final ListCropTypesUseCase listCropTypesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CropTypeView>>> getActiveCropTypes() {
        return ResponseEntity.ok(ApiResponse.<List<CropTypeView>>builder()
                .message("Crop types retrieved successfully")
                .result(listCropTypesUseCase.execute(true))
                .build());
    }
}

