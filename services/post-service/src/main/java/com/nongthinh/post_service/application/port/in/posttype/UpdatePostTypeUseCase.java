package com.nongthinh.post_service.application.port.in.posttype;

import com.nongthinh.post_service.application.command.UpdatePostTypeCommand;
import com.nongthinh.post_service.application.view.PostTypeView;
import java.util.UUID;

public interface UpdatePostTypeUseCase {
    PostTypeView execute(UUID id, UpdatePostTypeCommand command);
}
