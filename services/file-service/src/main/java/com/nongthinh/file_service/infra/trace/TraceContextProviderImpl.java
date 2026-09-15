package com.nongthinh.file_service.infra.trace;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.nongthinh.file_service.common.trace.TraceContextProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

@Component
public class TraceContextProviderImpl implements TraceContextProvider {

    @Override
    public Optional<String> currentTraceId() {
        SpanContext spanContext = Span.current().getSpanContext();
        if (!spanContext.isValid()) {
            return Optional.empty();
        }
        return Optional.of(spanContext.getTraceId());
    }
}
