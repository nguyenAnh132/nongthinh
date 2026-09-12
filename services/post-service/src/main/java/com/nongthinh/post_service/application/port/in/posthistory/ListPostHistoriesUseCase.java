package com.nongthinh.post_service.application.port.in.posthistory;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostHistoryView;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import java.util.UUID;

public interface ListPostHistoriesUseCase {
    PageView<PostHistoryView> execute(
            UUID postId,
            UUID postAuthorUserId,
            UUID actorUserId,
            HistoryActorType actorType,
            PostHistoryAction action,
            int page,
            int size
    );
}
