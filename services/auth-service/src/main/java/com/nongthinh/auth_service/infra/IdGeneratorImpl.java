package com.nongthinh.auth_service.infra;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.uuid.UuidCreator;
import com.nongthinh.auth_service.application.port.out.IdGenerator;

@Component
public class IdGeneratorImpl implements IdGenerator{

    @Override
    public UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }

}
