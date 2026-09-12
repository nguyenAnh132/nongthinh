package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.category.ListProductCategoriesUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/product-categories")
@RequiredArgsConstructor
public class PublicProductCategoryController {

    private final ListProductCategoriesUseCase listProductCategoriesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategoryView>>> getActiveProductCategories() {
        List<ProductCategoryView> result = listProductCategoriesUseCase.execute(true);
        return ResponseEntity.ok(ApiResponse.<List<ProductCategoryView>>builder()
                .message("Product categories retrieved successfully")
                .result(result)
                .build());
    }
}
