package com.nongthinh.post_service.application.port.in.postbookmark;

import com.nongthinh.post_service.application.view.PostBookmarkStatusView;
import java.util.UUID;

public interface GetPostBookmarkStatusUseCase {
    PostBookmarkStatusView execute(UUID postId);
}
