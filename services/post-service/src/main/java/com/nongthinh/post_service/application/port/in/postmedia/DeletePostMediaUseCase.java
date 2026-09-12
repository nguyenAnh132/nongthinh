package com.nongthinh.post_service.application.port.in.postmedia;

import java.util.UUID;

public interface DeletePostMediaUseCase {
    void execute(UUID postId, UUID mediaId);
}
