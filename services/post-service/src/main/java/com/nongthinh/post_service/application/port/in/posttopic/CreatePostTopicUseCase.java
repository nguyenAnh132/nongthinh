package com.nongthinh.post_service.application.port.in.posttopic;

import com.nongthinh.post_service.application.command.CreatePostTopicCommand;
import com.nongthinh.post_service.application.view.PostTopicView;

public interface CreatePostTopicUseCase {
    PostTopicView execute(CreatePostTopicCommand command);
}
