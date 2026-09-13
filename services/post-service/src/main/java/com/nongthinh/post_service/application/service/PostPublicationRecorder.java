package com.nongthinh.post_service.application.service;
import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.domain.post.Post;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class PostPublicationRecorder {
    private final PostEngagementRepository events;
    private final IdGenerator ids;
    public void record(Post post, Instant now) {
        if (!post.isVisibleInPublicFeed()) return;
        events.append(new PostEngagementEvent(ids.generate(), "post.published", 1, now,
                post.getId().value(), 1, Map.of(), post.getAuthorUserId().value(), null, null, true));
    }
}
