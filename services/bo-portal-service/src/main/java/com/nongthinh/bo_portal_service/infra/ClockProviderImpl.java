package com.nongthinh.bo_portal_service.infra;

import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ClockProviderImpl implements ClockProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
