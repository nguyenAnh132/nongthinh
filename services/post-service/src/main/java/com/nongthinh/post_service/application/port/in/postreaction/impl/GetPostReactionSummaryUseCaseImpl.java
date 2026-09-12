package com.nongthinh.post_service.application.port.in.postreaction.impl;

import com.nongthinh.post_service.application.port.out.PostEngagementRepository;

import com.nongthinh.post_service.application.port.in.postreaction.GetPostReactionSummaryUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReactionSummaryView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostReactionSummaryUseCaseImpl implements GetPostReactionSummaryUseCase {
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;
    private final PostEngagementRepository engagement;

    @Override
    public PostReactionSummaryView execute(UUID postId) {
        postSupport.requireInteractablePost(postId);
        var summary = engagement.snapshot(postId, currentUserProvider.findCurrentUserId().orElse(null));
        return new PostReactionSummaryView(
                summary.reactionTotal(), summary.reactionCounts(), summary.currentReaction(), summary.reactionVersion());
    }
}
