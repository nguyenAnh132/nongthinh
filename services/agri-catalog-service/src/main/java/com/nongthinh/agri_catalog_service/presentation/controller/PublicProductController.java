package com.nongthinh.agri_catalog_service.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.nongthinh.agri_catalog_service.application.port.in.product.SearchPublicProductsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.GetPublicProductUseCase;
import com.nongthinh.agri_catalog_service.application.query.PublicProductQuery;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.application.view.PublicProductDetailView;
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
    private final SearchPublicProductsUseCase searchPublicProductsUseCase;
    private final GetPublicProductUseCase getPublicProductUseCase;

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PageView<ProductView>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String diseaseName,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.<PageView<ProductView>>builder()
                .message("Products retrieved successfully")
                .result(searchPublicProductsUseCase.execute(
                        new PublicProductQuery(name, diseaseName, keyword, page))).build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<PublicProductDetailView>> getProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.<PublicProductDetailView>builder()
                .message("Product retrieved successfully").result(getPublicProductUseCase.execute(productId)).build());
    }

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
