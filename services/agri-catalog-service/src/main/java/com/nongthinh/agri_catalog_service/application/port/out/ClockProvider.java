package com.nongthinh.agri_catalog_service.application.port.out;

import java.time.Instant;

public interface ClockProvider {

    Instant now();
}
