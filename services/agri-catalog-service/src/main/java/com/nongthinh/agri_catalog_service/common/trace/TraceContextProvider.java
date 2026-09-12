package com.nongthinh.agri_catalog_service.common.trace;

import java.util.Optional;

public interface TraceContextProvider {

    Optional<String> currentTraceId();

}
