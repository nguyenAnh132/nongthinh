package com.nongthinh.post_service.application.port.in.postreaction.impl;

import com.nongthinh.post_service.application.service.PostEngagementRecorder;
import com.nongthinh.post_service.application.view.PostEngagementView;

import com.nongthinh.post_service.application.port.in.postreaction.RemovePostReactionUseCase;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemovePostReactionUseCaseImpl implements RemovePostReactionUseCase {
    private final ReactionRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;
    private final PostEngagementRecorder engagement;

    @Override
    @Transactional
    public PostEngagementView execute(UUID postId) {
        var post = postSupport.requirePostForUpdate(postId);
        postSupport.requireInteractablePost(postId);
        repository.deleteByPostIdAndActorId(postId, currentUserProvider.getCurrentUserId());
        return engagement.record(post, currentUserProvider.getCurrentUserId(), true, null, null);
    }
}
