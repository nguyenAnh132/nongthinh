package com.nongthinh.auth_service.infra.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.application.port.out.EventSerializer;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventSerializerImpl implements EventSerializer {

    private final ObjectMapper objectMapper;

    public EventSerializerImpl(@Qualifier("eventObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            log.error("Failed to serialize event: {}", event.getClass().getSimpleName(), ex);
            throw new InfrastructureException(ErrorCode.EVENT_SERIALIZATION_FAILED, ex);
        }
    }
}
