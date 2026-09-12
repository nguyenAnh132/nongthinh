package com.nongthinh.post_service.application.port.in.posttype;

import java.util.UUID;

public interface DeletePostTypeUseCase {
    void execute(UUID id);
}
