package com.nongthinh.post_service.application.port.in.post;

import java.util.UUID;

public interface DeletePostUseCase {
    void execute(UUID id);
}
