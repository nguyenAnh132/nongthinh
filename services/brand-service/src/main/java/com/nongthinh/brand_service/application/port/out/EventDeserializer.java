package com.nongthinh.brand_service.application.port.out;

public interface EventDeserializer {

    <T> T deserialize(String json, Class<T> type);
}
