package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import java.time.Instant;
import java.util.UUID;

public interface ListAdminPostsUseCase {
    PageView<PostView> execute(
            UUID authorUserId,
            PostStatus status,
            String keyword,
            Instant from,
            Instant to,
            int page,
            int size
    );
}
