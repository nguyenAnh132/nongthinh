package com.nongthinh.notification_service.infra;

import com.github.f4b6a3.uuid.UuidCreator;
import com.nongthinh.notification_service.application.port.out.IdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorImpl implements IdGenerator {

    @Override
    public UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
