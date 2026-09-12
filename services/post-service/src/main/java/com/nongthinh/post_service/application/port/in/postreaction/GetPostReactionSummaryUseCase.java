package com.nongthinh.post_service.application.port.in.postreaction;

import com.nongthinh.post_service.application.view.PostReactionSummaryView;
import java.util.UUID;

public interface GetPostReactionSummaryUseCase {
    PostReactionSummaryView execute(UUID postId);
}
