package com.nongthinh.auth_service.infra;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.nongthinh.auth_service.application.port.out.ClockProvider;

@Component
public class ClockProviderImpl implements ClockProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

    

}
