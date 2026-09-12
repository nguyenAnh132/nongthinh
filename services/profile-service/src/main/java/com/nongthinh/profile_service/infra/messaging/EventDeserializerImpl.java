package com.nongthinh.profile_service.infra.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.profile_service.application.port.out.EventDeserializer;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventDeserializerImpl implements EventDeserializer {

    private final ObjectMapper objectMapper;

    public EventDeserializerImpl(@Qualifier("eventObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            log.error("Failed to deserialize event to {}", type.getSimpleName(), ex);
            throw new BusinessException(ErrorCode.EVENT_DESERIALIZATION_FAILED, ex.getMessage());
        }
    }
}
