package com.nongthinh.post_service.application.port.in.posttopic;

import com.nongthinh.post_service.application.command.UpdatePostTopicCommand;
import com.nongthinh.post_service.application.view.PostTopicView;
import java.util.UUID;

public interface UpdatePostTopicUseCase {
    PostTopicView execute(UUID id, UpdatePostTopicCommand command);
}
