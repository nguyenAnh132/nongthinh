package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.domain.interaction.PostShare;
import java.util.UUID;

public interface PostShareRepository {
    PostShare save(PostShare share);
    long countByPostId(UUID postId);
}
