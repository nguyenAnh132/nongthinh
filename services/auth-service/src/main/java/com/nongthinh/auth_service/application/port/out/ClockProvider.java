package com.nongthinh.auth_service.application.port.out;

import java.time.Instant;

public interface ClockProvider {
    Instant now();
}
