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
import com.nongthinh.agri_catalog_service.application.port.in.productimage.CreateProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.DeleteProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.GetProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.ListProductImagesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.UpdateProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductImageCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductImageUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.ProductImageMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ListProductImagesUseCase listProductImagesUseCase;
    private final GetProductImageUseCase getProductImageUseCase;
    private final CreateProductImageUseCase createProductImageUseCase;
    private final UpdateProductImageUseCase updateProductImageUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;
    private final ProductImageMapper productImageMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductImageView>>> getProductImages(
            @PathVariable UUID productId
    ) {
        List<ProductImageView> result = listProductImagesUseCase.execute(productId, false);
        return ResponseEntity.ok(ApiResponse.<List<ProductImageView>>builder()
                .message("Product images retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageView>> getProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId
    ) {
        ProductImageView result = getProductImageUseCase.execute(productId, imageId);
        return ResponseEntity.ok(ApiResponse.<ProductImageView>builder()
                .message("Product image retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductImageView>> createProductImage(
            @PathVariable UUID productId,
            @RequestBody @Valid ProductImageCreationRequest request
    ) {
        ProductImageView result = createProductImageUseCase.execute(
                productId,
                productImageMapper.toProductImageCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductImageView>builder()
                        .message("Product image created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageView>> updateProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            @RequestBody @Valid ProductImageUpdateRequest request
    ) {
        ProductImageView result = updateProductImageUseCase.execute(
                productId,
                imageId,
                productImageMapper.toProductImageUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<ProductImageView>builder()
                .message("Product image updated successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId
    ) {
        deleteProductImageUseCase.execute(productId, imageId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Product image deleted successfully")
                .build());
    }
}
