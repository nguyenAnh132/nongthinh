package com.nongthinh.post_service.application.port.in.posttype;

import com.nongthinh.post_service.application.view.PostTypeView;
import java.util.UUID;

public interface GetPostTypeUseCase {
    PostTypeView execute(UUID id);
}
