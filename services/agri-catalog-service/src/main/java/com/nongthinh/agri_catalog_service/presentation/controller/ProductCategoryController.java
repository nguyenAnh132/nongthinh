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
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.agri_catalog_service.application.port.in.category.CreateProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.category.DeleteProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.category.GetProductCategoryByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.category.ListProductCategoriesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.category.UpdateProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCategoryCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCategoryUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.ProductCategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ListProductCategoriesUseCase listProductCategoriesUseCase;
    private final GetProductCategoryByIdUseCase getProductCategoryByIdUseCase;
    private final CreateProductCategoryUseCase createProductCategoryUseCase;
    private final UpdateProductCategoryUseCase updateProductCategoryUseCase;
    private final DeleteProductCategoryUseCase deleteProductCategoryUseCase;
    private final ProductCategoryMapper productCategoryMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductCategoryView>>> getAllProductCategories() {
        List<ProductCategoryView> result = listProductCategoriesUseCase.execute(false);
        return ResponseEntity.ok(ApiResponse.<List<ProductCategoryView>>builder()
                .message("Product categories retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategoryView>> getProductCategoryById(
            @PathVariable UUID id
    ) {
        ProductCategoryView result = getProductCategoryByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<ProductCategoryView>builder()
                .message("Product category retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategoryView>> createProductCategory(
            @RequestBody @Valid ProductCategoryCreationRequest request
    ) {
        ProductCategoryView result = createProductCategoryUseCase.execute(
                productCategoryMapper.toProductCategoryCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductCategoryView>builder()
                        .message("Product category created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategoryView>> updateProductCategory(
            @PathVariable UUID id,
            @RequestBody @Valid ProductCategoryUpdateRequest request
    ) {
        ProductCategoryView result = updateProductCategoryUseCase.execute(
                id,
                productCategoryMapper.toProductCategoryUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<ProductCategoryView>builder()
                .message("Product category updated successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProductCategory(@PathVariable UUID id) {
        deleteProductCategoryUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Product category deleted successfully")
                .build());
    }
}
