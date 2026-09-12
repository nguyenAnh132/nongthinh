package com.nongthinh.bo_portal_service.presentation.controller;

import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.CreateSystemParamTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.GetSystemParamTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.ListSystemParamTypesUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.ListSystemParamsGroupedByTypeUseCase;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeGroupView;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;
import com.nongthinh.bo_portal_service.presentation.dto.request.CreateSystemParamTypeRequest;
import com.nongthinh.bo_portal_service.presentation.mapper.SystemParamTypeMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("system-param-types")
@RequiredArgsConstructor
public class SystemParamTypeController {

    private final ListSystemParamTypesUseCase listSystemParamTypesUseCase;
    private final GetSystemParamTypeUseCase getSystemParamTypeUseCase;
    private final CreateSystemParamTypeUseCase createSystemParamTypeUseCase;
    private final ListSystemParamsGroupedByTypeUseCase listSystemParamsGroupedByTypeUseCase;
    private final SystemParamTypeMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<List<SystemParamTypeView>>> listSystemParamTypes() {
        List<SystemParamTypeView> data = listSystemParamTypesUseCase.execute();

        return ResponseEntity.ok(ApiResponse.<List<SystemParamTypeView>>builder()
                .message("System param types retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("grouped")
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<List<SystemParamTypeGroupView>>> listSystemParamsGroupedByType() {
        List<SystemParamTypeGroupView> data = listSystemParamsGroupedByTypeUseCase.execute();

        return ResponseEntity.ok(ApiResponse.<List<SystemParamTypeGroupView>>builder()
                .message("System params grouped by type retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<SystemParamTypeView>> getSystemParamType(@PathVariable Long id) {
        SystemParamTypeView data = getSystemParamTypeUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<SystemParamTypeView>builder()
                .message("System param type retrieved successfully")
                .result(Optional.ofNullable(data))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<SystemParamTypeView>> createSystemParamType(
            @RequestBody @Valid CreateSystemParamTypeRequest request) {
        SystemParamTypeView data = createSystemParamTypeUseCase.execute(mapper.toCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SystemParamTypeView>builder()
                        .message("System param type created successfully")
                        .result(Optional.ofNullable(data))
                        .build());
    }
}
