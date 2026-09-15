package com.nongthinh.profile_service.common.trace;

import java.util.Optional;

public interface TraceContextProvider {

    Optional<String> currentTraceId();
}
