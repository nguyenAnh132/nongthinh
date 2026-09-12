package com.nongthinh.profile_service.application.port.out;

import java.time.Instant;

public interface ClockProvider {

    Instant now();

}
