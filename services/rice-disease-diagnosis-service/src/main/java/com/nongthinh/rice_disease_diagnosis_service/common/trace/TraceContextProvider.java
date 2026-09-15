package com.nongthinh.rice_disease_diagnosis_service.common.trace;

import java.util.Optional;

public interface TraceContextProvider {

    Optional<String> currentTraceId();
}
