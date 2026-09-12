package com.nongthinh.bo_portal_service.presentation.controller;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.CreateSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.DeleteSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.GetSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.ListSystemParamsUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.UpdateSystemParamTypeAssignmentUseCase;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.UpdateSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;
import com.nongthinh.bo_portal_service.presentation.dto.request.CreateSystemParamRequest;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateSystemParamRequest;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateSystemParamTypeAssignmentRequest;
import com.nongthinh.bo_portal_service.presentation.mapper.SystemParamMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("system-params")
@RequiredArgsConstructor
public class SystemParamController {

    private final ListSystemParamsUseCase listSystemParamsUseCase;
    private final GetSystemParamUseCase getSystemParamUseCase;
    private final CreateSystemParamUseCase createSystemParamUseCase;
    private final UpdateSystemParamUseCase updateSystemParamUseCase;
    private final UpdateSystemParamTypeAssignmentUseCase updateSystemParamTypeAssignmentUseCase;
    private final DeleteSystemParamUseCase deleteSystemParamUseCase;
    private final SystemParamMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<List<SystemParamView>>> listSystemParams() {

        List<SystemParamView> data = listSystemParamsUseCase.execute();

        return ResponseEntity.ok(ApiResponse.<List<SystemParamView>>builder()
                .message("System params retrieved successfully")
                .result(Optional.of(data))
                .build());
    }

    @GetMapping("{name}")
    @PreAuthorize("hasAuthority('system:config:read')")
    public ResponseEntity<ApiResponse<SystemParamView>> getSystemParam(@PathVariable String name) {
        SystemParamView systemParam = getSystemParamUseCase.execute(name);
        return ResponseEntity.ok(ApiResponse.<SystemParamView>builder()
                .message("System param retrieved successfully")
                .result(Optional.ofNullable(systemParam))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<SystemParamView>> createSystemParam(
            @RequestBody @Valid CreateSystemParamRequest request) {
        SystemParamView systemParamView = createSystemParamUseCase.execute(mapper.toCreateCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SystemParamView>builder()
                        .message("System param created successfully")
                        .result(Optional.ofNullable(systemParamView))
                        .build());
    }

    @PutMapping("{name}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<SystemParamView>> updateSystemParam(
            @PathVariable String name,
            @RequestBody @Valid UpdateSystemParamRequest request) {
        SystemParamView systemParamView = updateSystemParamUseCase.execute(name, mapper.toUpdateCommand(request));

        return ResponseEntity.ok(ApiResponse.<SystemParamView>builder()
                .message("System param updated successfully")
                .result(Optional.ofNullable(systemParamView))
                .build());
    }

    @PutMapping("{name}/type")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<SystemParamView>> updateSystemParamType(
            @PathVariable String name,
            @RequestBody @Valid UpdateSystemParamTypeAssignmentRequest request) {
        SystemParamView systemParamView = updateSystemParamTypeAssignmentUseCase.execute(
                name,
                mapper.toUpdateTypeAssignmentCommand(request));

        return ResponseEntity.ok(ApiResponse.<SystemParamView>builder()
                .message("System param type updated successfully")
                .result(Optional.ofNullable(systemParamView))
                .build());
    }

    @DeleteMapping("{name}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<Void>> deleteSystemParam(@PathVariable String name) {
        deleteSystemParamUseCase.execute(name);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("System param deleted successfully")
                .result(Optional.empty())
                .build());
    }
}
