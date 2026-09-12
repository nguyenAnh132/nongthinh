package com.nongthinh.profile_service.application.port.out;

public interface EventDeserializer {

    <T> T deserialize(String json, Class<T> type);
}
