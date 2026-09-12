package com.nongthinh.post_service.infra;

import com.nongthinh.post_service.application.port.out.IdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorImpl implements IdGenerator {
    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
