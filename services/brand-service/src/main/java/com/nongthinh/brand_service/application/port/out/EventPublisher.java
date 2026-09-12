package com.nongthinh.brand_service.application.port.out;

import com.nongthinh.brand_service.application.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);
}
