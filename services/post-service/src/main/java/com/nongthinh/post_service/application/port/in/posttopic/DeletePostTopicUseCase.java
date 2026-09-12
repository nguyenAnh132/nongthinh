package com.nongthinh.post_service.application.port.in.posttopic;

import java.util.UUID;

public interface DeletePostTopicUseCase {
    void execute(UUID id);
}
