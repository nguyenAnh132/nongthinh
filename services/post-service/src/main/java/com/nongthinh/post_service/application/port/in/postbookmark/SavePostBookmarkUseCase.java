package com.nongthinh.post_service.application.port.in.postbookmark;

import com.nongthinh.post_service.application.view.PostBookmarkView;
import java.util.UUID;

public interface SavePostBookmarkUseCase {
    PostBookmarkView execute(UUID postId);
}
