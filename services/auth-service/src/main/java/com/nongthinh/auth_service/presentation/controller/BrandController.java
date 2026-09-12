package com.nongthinh.auth_service.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.auth_service.application.command.RegisterBrandCommand;
import com.nongthinh.auth_service.application.port.in.user.RegisterBrandUseCase;
import com.nongthinh.auth_service.presentation.dto.request.RegisterBrandRequest;
import com.nongthinh.auth_service.presentation.mapper.BrandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

    private final RegisterBrandUseCase registerBrandUseCase;
    private final BrandMapper brandMapper;

    @PostMapping
    public ResponseEntity<Void> registerBrand(@RequestBody @Valid RegisterBrandRequest request) {
        log.info("Register brand");
        RegisterBrandCommand command = brandMapper.toRegisterBrandCommand(request);
        registerBrandUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
