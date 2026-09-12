package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductsUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ListProductsUseCase listProductsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductView>>> getPublishedProducts(
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) UUID categoryId
    ) {
        List<ProductView> result = listProductsUseCase.execute(brandId, categoryId, true);
        return ResponseEntity.ok(ApiResponse.<List<ProductView>>builder()
                .message("Products retrieved successfully")
                .result(result)
                .build());
    }
}
