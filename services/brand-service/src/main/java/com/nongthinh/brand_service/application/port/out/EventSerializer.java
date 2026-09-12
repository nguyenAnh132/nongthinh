package com.nongthinh.brand_service.application.port.out;

public interface EventSerializer {

    String serialize(Object event);
}
