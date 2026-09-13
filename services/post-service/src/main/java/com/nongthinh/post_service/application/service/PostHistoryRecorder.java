package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostHistoryRecorder {
    private final PostHistoryRepository repository;
    private final IdGenerator idGenerator;
    private final PostPublicationRecorder publicationRecorder;

    public void record(Post post, UUID actorId, PostHistoryAction action,
                       PostStatus previousStatus, PostVisibility previousVisibility,
                       Instant now) {
        repository.append(PostHistory.create(
                idGenerator.generate(), post.getId().value(), post.getAuthorUserId().value(),
                actorId, HistoryActorType.USER, action, previousStatus, post.getStatus(),
                previousVisibility, post.getVisibility(), null, null, null,
                PostSnapshot.from(post), now
        ));
        if (action == PostHistoryAction.CREATED || action == PostHistoryAction.PUBLISHED) {
            publicationRecorder.record(post, now);
        }
    }

    public void recordModeration(Post post, UUID moderatorId, PostHistoryAction action,
                                 PostStatus previousStatus, PostVisibility previousVisibility,
                                 String reasonCode, String reasonDetail, UUID reportId,
                                 Instant now) {
        repository.append(PostHistory.create(
                idGenerator.generate(), post.getId().value(), post.getAuthorUserId().value(),
                moderatorId, HistoryActorType.ADMIN, action, previousStatus, post.getStatus(),
                previousVisibility, post.getVisibility(), reasonCode, reasonDetail, reportId,
                PostSnapshot.from(post), now
        ));
    }
}
