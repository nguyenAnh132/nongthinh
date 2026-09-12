package com.nongthinh.auth_service.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.command.RegisterAdminCommand;
import com.nongthinh.auth_service.application.port.in.user.RegisterAdminUseCase;
import com.nongthinh.auth_service.presentation.dto.request.RegisterAdminRequest;
import com.nongthinh.auth_service.presentation.mapper.AdminMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admins")
@RequiredArgsConstructor
public class AdminController {

    private final RegisterAdminUseCase registerAdminUseCase;
    private final AdminMapper adminMapper;

    @PostMapping
    @PreAuthorize("hasAuthority('admin:role:manage')")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid RegisterAdminRequest request) {
        RegisterAdminCommand command = adminMapper.toRegisterAdminCommand(request);
        registerAdminUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
