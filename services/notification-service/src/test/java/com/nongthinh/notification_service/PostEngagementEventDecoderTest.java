package com.nongthinh.notification_service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.nongthinh.notification_service.infra.messaging.PostEngagementEventDecoder;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class PostEngagementEventDecoderTest {
    private final PostEngagementEventDecoder decoder = new PostEngagementEventDecoder(new ObjectMapper());

    @Test
    void nullEnvelopeIsRejectedAsNonRetryableValidationFailure() {
        assertThrows(IllegalArgumentException.class, () -> decoder.decode("null"));
    }

    @Test
    void missingEventTypeIsRejectedAsNonRetryableValidationFailure() {
        assertThrows(IllegalArgumentException.class, () -> decoder.decode("""
                {"eventId":"00000000-0000-0000-0000-000000000001","schemaVersion":1,
                 "occurredAt":"2026-09-09T00:00:00Z","postId":"00000000-0000-0000-0000-000000000002",
                 "actorUserId":"00000000-0000-0000-0000-000000000003","version":1,"payload":{},"publicEngagement":true}
                """));
    }

    @Test
    void reportModerationNotificationIsAccepted() {
        assertDoesNotThrow(() -> decoder.decode("""
                {"eventId":"00000000-0000-0000-0000-000000000001",
                 "eventType":"post.report.resolved","schemaVersion":1,
                 "occurredAt":"2026-09-09T00:00:00Z",
                 "postId":"00000000-0000-0000-0000-000000000002",
                 "actorUserId":"00000000-0000-0000-0000-000000000003",
                 "recipientUserId":"00000000-0000-0000-0000-000000000004",
                 "notificationType":"POST_HIDDEN","version":1,
                 "payload":{"reportId":"00000000-0000-0000-0000-000000000005"},
                 "publicEngagement":false}
                """));
    }
}
