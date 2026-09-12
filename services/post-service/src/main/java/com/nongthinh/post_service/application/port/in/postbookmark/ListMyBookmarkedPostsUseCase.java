package com.nongthinh.post_service.application.port.in.postbookmark;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;

public interface ListMyBookmarkedPostsUseCase {
    PageView<PostView> execute(int page, int size);
}
