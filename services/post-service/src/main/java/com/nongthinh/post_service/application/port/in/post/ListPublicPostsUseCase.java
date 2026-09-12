package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import java.util.UUID;

public interface ListPublicPostsUseCase {
    PageView<PostView> execute(UUID postTypeId, UUID topicId, UUID cropTypeId,
                               String keyword, int page, int size);
}
