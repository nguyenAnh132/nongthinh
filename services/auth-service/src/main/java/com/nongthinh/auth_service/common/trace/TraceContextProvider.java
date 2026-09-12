package com.nongthinh.auth_service.common.trace;

import java.util.Optional;

public interface TraceContextProvider {

    Optional<String> currentTraceId();

}
