package com.nongthinh.location_service.presentation.controller;

import java.util.List;
import java.util.Optional;
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
import com.nongthinh.location_service.application.port.in.commune.ListCommunesByProvinceIdUseCase;
import com.nongthinh.location_service.application.port.in.province.CreateProvinceUseCase;
import com.nongthinh.location_service.application.port.in.province.DeleteProvinceUseCase;
import com.nongthinh.location_service.application.port.in.province.GetProvinceByIdUseCase;
import com.nongthinh.location_service.application.port.in.province.ListProvincesUseCase;
import com.nongthinh.location_service.application.port.in.province.UpdateProvinceUseCase;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.application.view.ProvinceView;
import com.nongthinh.location_service.common.response.ApiResponse;
import com.nongthinh.location_service.presentation.dto.request.ProvinceCreationRequest;
import com.nongthinh.location_service.presentation.dto.request.ProvinceUpdateRequest;
import com.nongthinh.location_service.presentation.mapper.ProvinceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ListProvincesUseCase listProvincesUseCase;
    private final GetProvinceByIdUseCase getProvinceByIdUseCase;
    private final ListCommunesByProvinceIdUseCase listCommunesByProvinceIdUseCase;
    private final CreateProvinceUseCase createProvinceUseCase;
    private final UpdateProvinceUseCase updateProvinceUseCase;
    private final DeleteProvinceUseCase deleteProvinceUseCase;
    private final ProvinceMapper provinceMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProvinceView>>> getAllProvinces() {
        List<ProvinceView> data = listProvincesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.<List<ProvinceView>>builder()
                .message("Provinces retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProvinceView>> getProvinceById(@PathVariable String id) {
        ProvinceView view = getProvinceByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<ProvinceView>builder()
                .message("Province retrieved successfully")
                .result(Optional.of(view))
                .build());
    }

    @GetMapping("/{provinceId}/communes")
    public ResponseEntity<ApiResponse<List<CommuneView>>> getCommunesByProvince(
            @PathVariable String provinceId
    ) {
        List<CommuneView> data = listCommunesByProvinceIdUseCase.execute(provinceId);
        return ResponseEntity.ok(ApiResponse.<List<CommuneView>>builder()
                .message("Communes retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<ProvinceView>> createProvince(
            @RequestBody @Valid ProvinceCreationRequest request
    ) {
        ProvinceView view = createProvinceUseCase.execute(provinceMapper.toProvinceCreationCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ProvinceView>builder()
                .message("Province created successfully")
                .result(Optional.of(view))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<ProvinceView>> updateProvince(
            @PathVariable String id,
            @RequestBody @Valid ProvinceUpdateRequest request
    ) {
        ProvinceView view = updateProvinceUseCase.execute(id, provinceMapper.toProvinceUpdateCommand(request));
        return ResponseEntity.ok(ApiResponse.<ProvinceView>builder()
                .message("Province updated successfully")
                .result(Optional.of(view))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<Void>> deleteProvince(@PathVariable String id) {
        deleteProvinceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Province deleted successfully")
                .build());
    }
}
