package com.nongthinh.auth_service.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.port.in.user.RegisterFarmerUseCase;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.nongthinh.auth_service.application.command.RegisterFarmerCommand;
import com.nongthinh.auth_service.presentation.dto.request.RegisterFarmerRequest;
import jakarta.validation.Valid;
import com.nongthinh.auth_service.presentation.mapper.FarmerMapper;

@RestController
@RequestMapping("farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final RegisterFarmerUseCase registerFarmerUseCase;
    private final FarmerMapper registerFarmerMapper;

    @PostMapping
    public ResponseEntity<Void> registerFarmer(@RequestBody @Valid RegisterFarmerRequest request) {
        RegisterFarmerCommand command = registerFarmerMapper.toRegisterFarmerCommand(request);
        registerFarmerUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
