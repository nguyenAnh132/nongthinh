package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import java.util.UUID;

public record PostHistoryFilter(
        UUID postId,
        UUID postAuthorUserId,
        UUID actorUserId,
        HistoryActorType actorType,
        PostHistoryAction action
) {
}
