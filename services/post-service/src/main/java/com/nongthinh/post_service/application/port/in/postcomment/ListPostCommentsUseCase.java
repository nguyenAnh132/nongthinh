package com.nongthinh.post_service.application.port.in.postcomment;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostCommentThreadView;
import java.util.UUID;

public interface ListPostCommentsUseCase {
    PageView<PostCommentThreadView> execute(UUID postId, int page, int size);
}
