package com.nongthinh.auth_service.application.port.out;

import com.nongthinh.auth_service.application.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);

}
