package com.nongthinh.post_service.application.port.in.posttopic;

import com.nongthinh.post_service.application.view.PostTopicView;
import java.util.List;

public interface ListPostTopicsUseCase {
    List<PostTopicView> execute(boolean activeOnly);
}
