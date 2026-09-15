package com.nongthinh.brand_service.common.trace;

import java.util.Optional;

public interface TraceContextProvider {

    Optional<String> currentTraceId();
}
