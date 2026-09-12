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
import com.nongthinh.agri_catalog_service.application.port.in.croptype.CreateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.DeleteCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.GetCropTypeByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.ListCropTypesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.UpdateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.common.response.ApiResponse;
import com.nongthinh.agri_catalog_service.presentation.dto.request.CropTypeCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.CropTypeUpdateRequest;
import com.nongthinh.agri_catalog_service.presentation.mapper.CropTypeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("crop-types")
@RequiredArgsConstructor
public class CropTypeController {

    private final ListCropTypesUseCase listCropTypesUseCase;
    private final GetCropTypeByIdUseCase getCropTypeByIdUseCase;
    private final CreateCropTypeUseCase createCropTypeUseCase;
    private final UpdateCropTypeUseCase updateCropTypeUseCase;
    private final DeleteCropTypeUseCase deleteCropTypeUseCase;
    private final CropTypeMapper cropTypeMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<CropTypeView>>> getCropTypes() {
        return ResponseEntity.ok(ApiResponse.<List<CropTypeView>>builder()
                .message("Crop types retrieved successfully")
                .result(listCropTypesUseCase.execute(false))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CropTypeView>> getCropTypeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<CropTypeView>builder()
                .message("Crop type retrieved successfully")
                .result(getCropTypeByIdUseCase.execute(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CropTypeView>> createCropType(
            @RequestBody @Valid CropTypeCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CropTypeView>builder()
                        .message("Crop type created successfully")
                        .result(createCropTypeUseCase.execute(
                                cropTypeMapper.toCropTypeCreationCommand(request)
                        ))
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CropTypeView>> updateCropType(
            @PathVariable UUID id,
            @RequestBody @Valid CropTypeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<CropTypeView>builder()
                .message("Crop type updated successfully")
                .result(updateCropTypeUseCase.execute(
                        id,
                        cropTypeMapper.toCropTypeUpdateCommand(request)
                ))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCropType(@PathVariable UUID id) {
        deleteCropTypeUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Crop type deleted successfully")
                .build());
    }
}

