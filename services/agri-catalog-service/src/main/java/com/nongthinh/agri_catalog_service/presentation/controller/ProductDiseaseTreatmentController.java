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
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.CreateProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.DeleteProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.GetProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.ListProductDiseaseTreatmentsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.UpdateProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductDiseaseTreatmentCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductDiseaseTreatmentUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.ProductDiseaseTreatmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("products/{productId}/disease-treatments")
@RequiredArgsConstructor
public class ProductDiseaseTreatmentController {

    private final ListProductDiseaseTreatmentsUseCase listTreatmentsUseCase;
    private final GetProductDiseaseTreatmentUseCase getTreatmentUseCase;
    private final CreateProductDiseaseTreatmentUseCase createTreatmentUseCase;
    private final UpdateProductDiseaseTreatmentUseCase updateTreatmentUseCase;
    private final DeleteProductDiseaseTreatmentUseCase deleteTreatmentUseCase;
    private final ProductDiseaseTreatmentMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<List<ProductDiseaseTreatmentView>>> getTreatments(
            @PathVariable UUID productId
    ) {
        List<ProductDiseaseTreatmentView> result =
                listTreatmentsUseCase.execute(productId, false);
        return ResponseEntity.ok(ApiResponse.<List<ProductDiseaseTreatmentView>>builder()
                .message("Product disease treatments retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{treatmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductDiseaseTreatmentView>> getTreatment(
            @PathVariable UUID productId,
            @PathVariable UUID treatmentId
    ) {
        ProductDiseaseTreatmentView result =
                getTreatmentUseCase.execute(productId, treatmentId);
        return ResponseEntity.ok(ApiResponse.<ProductDiseaseTreatmentView>builder()
                .message("Product disease treatment retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductDiseaseTreatmentView>> createTreatment(
            @PathVariable UUID productId,
            @RequestBody @Valid ProductDiseaseTreatmentCreationRequest request
    ) {
        ProductDiseaseTreatmentView result = createTreatmentUseCase.execute(
                productId,
                mapper.toCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductDiseaseTreatmentView>builder()
                        .message("Product disease treatment created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{treatmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<ProductDiseaseTreatmentView>> updateTreatment(
            @PathVariable UUID productId,
            @PathVariable UUID treatmentId,
            @RequestBody @Valid ProductDiseaseTreatmentUpdateRequest request
    ) {
        ProductDiseaseTreatmentView result = updateTreatmentUseCase.execute(
                productId,
                treatmentId,
                mapper.toUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<ProductDiseaseTreatmentView>builder()
                .message("Product disease treatment updated successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{treatmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<Void>> deleteTreatment(
            @PathVariable UUID productId,
            @PathVariable UUID treatmentId
    ) {
        deleteTreatmentUseCase.execute(productId, treatmentId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Product disease treatment deleted successfully")
                .build());
    }
}
