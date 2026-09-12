package com.nongthinh.agri_catalog_service.infra;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.agri_catalog_service.application.port.out.SnapshotSerializer;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SnapshotSerializerImpl implements SnapshotSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.EVENT_SERIALIZATION_FAILED, ex);
        }
    }
}
