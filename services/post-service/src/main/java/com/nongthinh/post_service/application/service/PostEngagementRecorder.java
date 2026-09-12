package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.application.view.PostEngagementView;
import com.nongthinh.post_service.domain.post.Post;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostEngagementRecorder {
    private final PostEngagementRepository repository;
    private final IdGenerator ids;
    private final ClockProvider clock;

    public PostEngagementView record(Post post, UUID actorId, boolean reaction,
                                     UUID recipientId, String notificationType) {
        UUID postId = post.getId().value();
        repository.incrementVersion(postId, reaction);
        PostEngagementView summary = repository.snapshot(postId, actorId);
        Map<String, Object> payload = reaction
                ? Map.of("postId", postId, "reactionCounts", summary.reactionCounts(),
                        "reactionTotal", summary.reactionTotal(), "reactionVersion", summary.reactionVersion())
                : Map.of("postId", postId, "commentRootTotal", summary.commentRootTotal(),
                        "commentTotal", summary.commentTotal(), "commentVersion", summary.commentVersion());
        repository.append(new PostEngagementEvent(ids.generate(),
                reaction ? "post.reaction.updated" : "post.comment.updated", 1, clock.now(),
                postId, reaction ? summary.reactionVersion() : summary.commentVersion(), payload,
                actorId, recipientId, notificationType, post.isVisibleInPublicFeed()));
        return summary;
    }
}
