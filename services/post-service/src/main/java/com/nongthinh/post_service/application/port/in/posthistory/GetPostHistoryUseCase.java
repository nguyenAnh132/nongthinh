package com.nongthinh.post_service.application.port.in.posthistory;

import com.nongthinh.post_service.application.view.PostHistoryView;
import java.util.UUID;

public interface GetPostHistoryUseCase {
    PostHistoryView execute(UUID historyId);
}
