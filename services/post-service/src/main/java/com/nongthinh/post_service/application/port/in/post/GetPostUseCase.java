package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.view.PostView;
import java.util.UUID;

public interface GetPostUseCase {
    PostView execute(UUID id);
}
