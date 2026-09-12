package com.nongthinh.bo_portal_service.presentation.controller;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.GetSystemParamValueUseCase;
import com.nongthinh.bo_portal_service.application.view.SystemParamValueView;
import com.nongthinh.bo_portal_service.common.response.ApiResponse;
import com.nongthinh.bo_portal_service.presentation.mapper.SystemParamMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("internal/system-params")
@RequiredArgsConstructor
public class InternalSystemParamController {

    private final GetSystemParamValueUseCase getSystemParamValueUseCase;

    @GetMapping("{name}")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL')")
    public ResponseEntity<ApiResponse<SystemParamValueView>> getSystemParam(@PathVariable String name) {
        SystemParamValueView systemParamValueView = getSystemParamValueUseCase.execute(name);

        return ResponseEntity.ok(ApiResponse.<SystemParamValueView>builder()
                .message("System param retrieved successfully")
                .result(Optional.of(systemParamValueView))
                .build());
    }
}
