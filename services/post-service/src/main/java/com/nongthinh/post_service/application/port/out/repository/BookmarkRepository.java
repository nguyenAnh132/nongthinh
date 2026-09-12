package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.domain.interaction.Bookmark;
import java.util.UUID;

public interface BookmarkRepository {
    boolean exists(UUID postId, UUID userId);
    Bookmark save(Bookmark bookmark);
    void delete(UUID postId, UUID userId);
}
