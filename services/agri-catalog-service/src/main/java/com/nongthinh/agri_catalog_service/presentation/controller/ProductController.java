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
import com.nongthinh.agri_catalog_service.application.port.in.product.CreateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.DeleteProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.GetProductByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductPublicationStatusUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductHistoryView;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.ProductMapper;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {

    private final ListProductsUseCase listProductsUseCase;
    private final ListProductHistoryUseCase listProductHistoryUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final UpdateProductPublicationStatusUseCase updateProductPublicationStatusUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ProductMapper productMapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<List<ProductView>>> getProducts(
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) UUID categoryId
    ) {
        List<ProductView> result = listProductsUseCase.execute(brandId, categoryId, false);
        return ResponseEntity.ok(ApiResponse.<List<ProductView>>builder()
                .message("Products retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<List<ProductHistoryView>>> getProductHistory(
            @PathVariable UUID id
    ) {
        List<ProductHistoryView> result = listProductHistoryUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<List<ProductHistoryView>>builder()
                .message("Product history retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductView>> getProductById(@PathVariable UUID id) {
        ProductView result = getProductByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<ProductView>builder()
                .message("Product retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductView>> createProduct(
            @RequestBody @Valid ProductCreationRequest request
    ) {
        ProductView result = createProductUseCase.execute(
                productMapper.toProductCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductView>builder()
                        .message("Product created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductView>> updateProduct(
            @PathVariable UUID id,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        ProductView result = updateProductUseCase.execute(
                id,
                productMapper.toProductUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<ProductView>builder()
                .message("Product updated successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductView>> publishProduct(@PathVariable UUID id) {
        ProductView result = updateProductPublicationStatusUseCase.execute(id, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.<ProductView>builder()
                .message("Product published successfully")
                .result(result)
                .build());
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductView>> unpublishProduct(@PathVariable UUID id) {
        ProductView result = updateProductPublicationStatusUseCase.execute(id, PublicationStatus.UNPUBLISHED);
        return ResponseEntity.ok(ApiResponse.<ProductView>builder()
                .message("Product unpublished successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Product deleted successfully")
                .build());
    }
}
