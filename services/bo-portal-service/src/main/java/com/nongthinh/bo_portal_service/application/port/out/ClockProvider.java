package com.nongthinh.bo_portal_service.application.port.out;

import java.time.Instant;

public interface ClockProvider {

    Instant now();
}
