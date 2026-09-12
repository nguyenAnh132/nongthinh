package com.nongthinh.auth_service.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("trace")
    public ResponseEntity<Void> trace() {
        log.info("trace");
        return ResponseEntity.ok().build();
    }

}
