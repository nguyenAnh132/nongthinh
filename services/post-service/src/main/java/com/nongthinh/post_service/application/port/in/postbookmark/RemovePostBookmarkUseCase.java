package com.nongthinh.post_service.application.port.in.postbookmark;

import java.util.UUID;

public interface RemovePostBookmarkUseCase {
    void execute(UUID postId);
}
