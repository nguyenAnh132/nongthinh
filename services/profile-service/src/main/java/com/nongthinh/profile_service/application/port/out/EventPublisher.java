package com.nongthinh.profile_service.application.port.out;

import com.nongthinh.profile_service.application.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);

}
