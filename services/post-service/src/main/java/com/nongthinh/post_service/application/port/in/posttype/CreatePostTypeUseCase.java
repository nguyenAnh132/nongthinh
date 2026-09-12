package com.nongthinh.post_service.application.port.in.posttype;

import com.nongthinh.post_service.application.command.CreatePostTypeCommand;
import com.nongthinh.post_service.application.view.PostTypeView;

public interface CreatePostTypeUseCase {
    PostTypeView execute(CreatePostTypeCommand command);
}
