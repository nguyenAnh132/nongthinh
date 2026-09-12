package com.nongthinh.profile_service.infra;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.nongthinh.profile_service.application.port.out.ClockProvider;

@Component
public class ClockProviderImpl implements ClockProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

    

}
