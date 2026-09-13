package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PostEngagementEventDecoder {
    private final ObjectMapper json;
    public PostEngagementEvent decode(String value) {
        PostEngagementEvent event = json.readValue(value, PostEngagementEvent.class);
        if (event == null || event.schemaVersion() != 1 || event.eventId() == null || event.postId() == null
                || event.occurredAt() == null || event.actorUserId() == null || event.version() < 1
                || event.payload() == null || event.eventType() == null
                || !Set.of("post.reaction.updated", "post.comment.updated",
                           "post.report.resolved", "post.report.rejected", "post.published").contains(event.eventType())
                || (event.notificationType() != null
                    && !Set.of("POST_REACTION", "POST_COMMENT", "COMMENT_REPLY",
                               "REPORT_RESOLVED", "REPORT_REJECTED", "POST_HIDDEN",
                               "POST_DELETED", "POST_REPORT_REJECTED")
                            .contains(event.notificationType())))
            throw new IllegalArgumentException("Invalid post engagement event");
        return event;
    }
}
