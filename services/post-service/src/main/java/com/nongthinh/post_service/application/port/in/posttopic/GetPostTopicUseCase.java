package com.nongthinh.post_service.application.port.in.posttopic;

import com.nongthinh.post_service.application.view.PostTopicView;
import java.util.UUID;

public interface GetPostTopicUseCase {
    PostTopicView execute(UUID id);
}
