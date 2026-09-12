package com.nongthinh.post_service.application.port.in.postshare;

import com.nongthinh.post_service.application.view.PostShareView;
import java.util.UUID;

public interface CreatePostShareUseCase {
    PostShareView execute(UUID postId);
}
