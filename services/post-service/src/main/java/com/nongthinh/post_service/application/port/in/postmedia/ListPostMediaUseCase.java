package com.nongthinh.post_service.application.port.in.postmedia;

import com.nongthinh.post_service.application.view.PostMediaView;
import java.util.List;
import java.util.UUID;

public interface ListPostMediaUseCase {
    List<PostMediaView> execute(UUID postId);
}
