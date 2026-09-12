package com.nongthinh.post_service.application.port.in.postshare.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostShareRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.domain.interaction.PostShare;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostShareUseCasesTest {
    private static final UUID POST_ID = UUID.fromString("c24f4bc7-90f0-42d0-904d-c98716270f8f");
    private static final UUID USER_ID = UUID.fromString("a2e1713b-0d8c-4d51-9023-0ee0a878ee5c");
    private static final UUID SHARE_ID = UUID.fromString("1f692c1a-457b-4864-a23c-eaa2cbc05aa7");
    private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");

    @Mock private PostShareRepository repository;
    @Mock private PostUseCaseSupport postSupport;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;

    @Test
    void recordsEachShareForCurrentUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(idGenerator.generate()).thenReturn(SHARE_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(repository.save(any(PostShare.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostShareUseCaseImpl(
                repository, postSupport, currentUserProvider, idGenerator, clockProvider)
                .execute(POST_ID);

        assertThat(view.id()).isEqualTo(SHARE_ID);
        assertThat(view.postId()).isEqualTo(POST_ID);
        assertThat(view.sharedByUserId()).isEqualTo(USER_ID);
        assertThat(view.createdAt()).isEqualTo(NOW);
        verify(postSupport).requireInteractablePost(POST_ID);
    }

    @Test
    void returnsShareCountForInteractablePost() {
        when(repository.countByPostId(POST_ID)).thenReturn(12L);

        var summary = new GetPostShareSummaryUseCaseImpl(repository, postSupport)
                .execute(POST_ID);

        assertThat(summary.postId()).isEqualTo(POST_ID);
        assertThat(summary.totalCount()).isEqualTo(12L);
        verify(postSupport).requireInteractablePost(POST_ID);
    }
}
