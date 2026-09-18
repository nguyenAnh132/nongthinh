package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.command.GetPostFeedCommand;
import com.nongthinh.post_service.application.view.PostFeedView;

public interface GetPostFeedUseCase {
    PostFeedView execute(GetPostFeedCommand command);
}
