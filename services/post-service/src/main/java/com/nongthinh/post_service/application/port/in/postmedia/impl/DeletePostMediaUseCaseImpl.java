package com.nongthinh.post_service.application.port.in.postmedia.impl;

import com.nongthinh.post_service.application.port.in.postmedia.DeletePostMediaUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostMediaUseCaseImpl implements DeletePostMediaUseCase {
    private final PostRepository repository;
    private final PostUseCaseSupport support;
    private final PostHistoryRecorder historyRecorder;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID postId, UUID mediaId) {
        Post post = support.requirePostForUpdate(postId);
        AuthorUserId actor = support.currentAuthor();
        support.assertOwner(post, actor);
        support.assertEditable(post);
        support.requireMedia(post, mediaId);
        PostStatus previousStatus = post.getStatus();
        PostVisibility previousVisibility = post.getVisibility();
        Instant now = clockProvider.now();
        post.removeMedia(actor, mediaId, support.maxMedia(), now);
        Post saved = repository.save(post);
        historyRecorder.record(saved, actor.value(), PostHistoryAction.UPDATED,
                previousStatus, previousVisibility, now);
    }
}
