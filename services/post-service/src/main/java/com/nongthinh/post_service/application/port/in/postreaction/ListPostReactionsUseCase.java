package com.nongthinh.post_service.application.port.in.postreaction;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReactionView;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.util.UUID;

public interface ListPostReactionsUseCase {
    PageView<PostReactionView> execute(UUID postId, ReactionType reactionType, int page, int size);
}
