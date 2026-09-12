package com.nongthinh.post_service.application.port.in.postreaction.impl;

import com.nongthinh.post_service.application.service.PostEngagementRecorder;

import com.nongthinh.post_service.application.command.SetPostReactionCommand;
import com.nongthinh.post_service.application.port.in.postreaction.SetPostReactionUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReactionView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.interaction.Reaction;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetPostReactionUseCaseImpl implements SetPostReactionUseCase {
    private final ReactionRepository repository;
    private final PostUseCaseSupport postSupport;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final PostEngagementRecorder engagement;

    @Override
    @Transactional
    public PostReactionView execute(UUID postId, SetPostReactionCommand command) {
        if (command == null || command.reactionType() == null) {
            throw new BusinessException(ErrorCode.REACTION_TYPE_REQUIRED);
        }
        var post = postSupport.requirePostForUpdate(postId);
        postSupport.requireInteractablePost(postId);
        Reaction reaction = Reaction.create(
                idGenerator.generate(), postId, currentUserProvider.getCurrentUserId(),
                command.reactionType(), clockProvider.now()
        );
        var saved = repository.upsert(reaction);
        var metrics = engagement.record(post, reaction.getActorId(), true,
                post.getAuthorUserId().value(), "POST_REACTION");
        return PostReactionView.from(saved).withMetrics(metrics);
    }
}
