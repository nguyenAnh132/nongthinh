package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.ListProductImagesUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public/products/{productId}/images")
@RequiredArgsConstructor
public class PublicProductImageController {

    private final ListProductImagesUseCase listProductImagesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageView>>> getProductImages(
            @PathVariable UUID productId
    ) {
        List<ProductImageView> result = listProductImagesUseCase.execute(productId, true);
        return ResponseEntity.ok(ApiResponse.<List<ProductImageView>>builder()
                .message("Product images retrieved successfully")
                .result(result)
                .build());
    }
}
