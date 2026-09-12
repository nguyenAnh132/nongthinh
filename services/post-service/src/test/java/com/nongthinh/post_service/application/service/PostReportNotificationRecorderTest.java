package com.nongthinh.post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostReportNotificationRecorderTest {
    private static final UUID POST_ID = UUID.fromString("c2960fea-7434-4ad7-a1e5-f42ed0b66830");
    private static final UUID REPORT_ID = UUID.fromString("1d42f792-222a-45b4-950e-3fdc6847c298");
    private static final UUID REPORTER_ID = UUID.fromString("c66fb37e-d105-4219-bfb0-f841c4583556");
    private static final UUID AUTHOR_ID = UUID.fromString("da05cd14-402c-4cc5-8ae9-160a2722a22f");
    private static final UUID MODERATOR_ID = UUID.fromString("ef216919-d81a-4978-93a6-95b185f7080f");
    private static final Instant NOW = Instant.parse("2026-09-12T00:00:00Z");

    @Mock private PostEngagementRepository repository;
    @Mock private IdGenerator ids;

    @Test
    void resolvedReportNotifiesReporterAndPostOwner() {
        PostReport report = PostReport.create(
                REPORT_ID, POST_ID, REPORTER_ID, ReportReason.SPAM, "Repeated ads", NOW);
        report.resolve(MODERATOR_ID, "Confirmed", NOW.plusSeconds(1));
        Post post = post();
        when(ids.generate()).thenReturn(UUID.randomUUID(), UUID.randomUUID());

        new PostReportNotificationRecorder(repository, ids).recordResolved(
                report, post, MODERATOR_ID, PostModerationAction.HIDE, NOW.plusSeconds(1));

        ArgumentCaptor<PostEngagementEvent> events =
                ArgumentCaptor.forClass(PostEngagementEvent.class);
        verify(repository, times(2)).append(events.capture());
        assertThat(events.getAllValues())
                .extracting(PostEngagementEvent::notificationType)
                .containsExactly("REPORT_RESOLVED", "POST_HIDDEN");
        assertThat(events.getAllValues())
                .extracting(PostEngagementEvent::recipientUserId)
                .containsExactly(REPORTER_ID, AUTHOR_ID);
        assertThat(events.getAllValues()).allSatisfy(event -> {
            assertThat(event.eventType()).isEqualTo("post.report.resolved");
            assertThat(event.publicEngagement()).isFalse();
            assertThat(event.payload()).containsEntry("reportId", REPORT_ID);
        });
    }

    @Test
    void rejectedReportNotifiesReporterAndPostOwnerWithoutModeratingThePost() {
        PostReport report = PostReport.create(
                REPORT_ID, POST_ID, REPORTER_ID, ReportReason.SPAM, "Repeated ads", NOW);
        report.reject(MODERATOR_ID, "No violation", NOW.plusSeconds(1));
        when(ids.generate()).thenReturn(UUID.randomUUID(), UUID.randomUUID());

        new PostReportNotificationRecorder(repository, ids).recordRejected(
                report, post(), MODERATOR_ID, NOW.plusSeconds(1));

        ArgumentCaptor<PostEngagementEvent> events =
                ArgumentCaptor.forClass(PostEngagementEvent.class);
        verify(repository, times(2)).append(events.capture());
        assertThat(events.getAllValues())
                .extracting(PostEngagementEvent::notificationType)
                .containsExactly("REPORT_REJECTED", "POST_REPORT_REJECTED");
        assertThat(events.getAllValues())
                .extracting(PostEngagementEvent::recipientUserId)
                .containsExactly(REPORTER_ID, AUTHOR_ID);
        assertThat(events.getAllValues())
                .allSatisfy(event -> assertThat(event.eventType())
                        .isEqualTo("post.report.rejected"));
    }

    private static Post post() {
        return Post.createPublished(
                new PostId(POST_ID), new AuthorUserId(AUTHOR_ID), null, null,
                new PostContent("Reported content"), null, PostVisibility.PUBLIC,
                List.of(), Set.of(), NOW, 10);
    }
}
