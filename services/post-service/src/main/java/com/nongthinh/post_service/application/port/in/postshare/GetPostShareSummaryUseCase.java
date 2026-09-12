package com.nongthinh.post_service.application.port.in.postshare;

import com.nongthinh.post_service.application.view.PostShareSummaryView;
import java.util.UUID;

public interface GetPostShareSummaryUseCase {
    PostShareSummaryView execute(UUID postId);
}
