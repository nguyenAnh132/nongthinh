package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;

public interface ListMyPostsUseCase {
    PageView<PostView> execute(PostStatus status, int page, int size);
}
