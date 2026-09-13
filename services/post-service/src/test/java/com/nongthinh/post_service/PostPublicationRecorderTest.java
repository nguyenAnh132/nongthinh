package com.nongthinh.post_service;
import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.application.service.PostPublicationRecorder;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class PostPublicationRecorderTest {
    @Test void publicPublicationCreatesRoutableOutboxEvent() {
        var repository = mock(PostEngagementRepository.class);
        var post = mock(Post.class);
        var id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var actor = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(post.isVisibleInPublicFeed()).thenReturn(true);
        when(post.getId()).thenReturn(new PostId(id));
        when(post.getAuthorUserId()).thenReturn(new AuthorUserId(actor));
        var now = Instant.parse("2026-09-12T00:00:00Z");
        new PostPublicationRecorder(repository, () -> id).record(post, now);
        var event = ArgumentCaptor.forClass(PostEngagementEvent.class);
        verify(repository).append(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo("post.published");
        assertThat(event.getValue().actorUserId()).isEqualTo(actor);
        assertThat(event.getValue().publicEngagement()).isTrue();
        assertThat(event.getValue().occurredAt()).isEqualTo(now);
    }
    @Test void draftsOrNonPublicPostsDoNotNotify() {
        var repository = mock(PostEngagementRepository.class);
        new PostPublicationRecorder(repository, () -> null).record(mock(Post.class), Instant.EPOCH);
        verifyNoInteractions(repository);
    }
}
