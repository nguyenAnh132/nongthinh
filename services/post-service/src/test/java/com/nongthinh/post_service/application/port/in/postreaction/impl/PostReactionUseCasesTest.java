package com.nongthinh.post_service.application.port.in.postreaction.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.SetPostReactionCommand;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostReactionUseCasesTest {
    private static final UUID POST_ID = UUID.fromString("78bab3eb-6613-42c3-9d95-e8be298ef242");
    private static final UUID ACTOR_ID = UUID.fromString("7c20c104-8a6d-408d-9f76-d86b5e8e5e1c");
    private static final UUID REACTION_ID = UUID.fromString("769f3526-d0f5-4636-956a-aa72f4f8f63e");
    private static final Instant NOW = Instant.parse("2026-08-24T00:00:00Z");

    @Mock private ReactionRepository repository;
    @Mock private PostUseCaseSupport postSupport;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    @Mock private com.nongthinh.post_service.application.service.PostEngagementRecorder engagement;
    @Mock private com.nongthinh.post_service.application.port.out.PostEngagementRepository metricsRepository;
    @Mock private com.nongthinh.post_service.domain.post.Post post;

    @org.junit.jupiter.api.BeforeEach
    void setupEngagement() {
        org.mockito.Mockito.lenient().when(postSupport.requirePostForUpdate(POST_ID)).thenReturn(post);
        org.mockito.Mockito.lenient().when(post.getAuthorUserId()).thenReturn(
                new com.nongthinh.post_service.domain.post.valueobject.AuthorUserId(ACTOR_ID));
        org.mockito.Mockito.lenient().when(engagement.record(any(), any(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.nullable(UUID.class), org.mockito.ArgumentMatchers.nullable(String.class)))
                .thenReturn(new com.nongthinh.post_service.application.view.PostEngagementView(
                        POST_ID, Map.of(ReactionType.LOVE, 1L), 1, 7, ReactionType.LOVE, 0, 0, 0));
    }

    @Test
    void atomicallySetsReactionForCurrentUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(ACTOR_ID);
        when(idGenerator.generate()).thenReturn(REACTION_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(repository.upsert(any(Reaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var view = new SetPostReactionUseCaseImpl(
                repository, postSupport, currentUserProvider, idGenerator, clockProvider, engagement)
                .execute(POST_ID, new SetPostReactionCommand(ReactionType.LOVE));

        assertThat(view.id()).isEqualTo(REACTION_ID);
        assertThat(view.actorId()).isEqualTo(ACTOR_ID);
        assertThat(view.reactionType()).isEqualTo(ReactionType.LOVE);
        assertThat(view.reactionVersion()).isEqualTo(7);
        assertThat(view.reactionTotal()).isEqualTo(1);
        var ordered = org.mockito.Mockito.inOrder(postSupport, repository, engagement);
        ordered.verify(postSupport).requirePostForUpdate(POST_ID);
        ordered.verify(repository).upsert(any());
        ordered.verify(engagement).record(post, ACTOR_ID, true, ACTOR_ID, "POST_REACTION");
        verify(postSupport).requireInteractablePost(POST_ID);
    }

    @Test
    void summaryContainsEveryReactionTypeAndCurrentUsersReaction() {
        when(currentUserProvider.findCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(metricsRepository.snapshot(POST_ID, ACTOR_ID)).thenReturn(
                new com.nongthinh.post_service.application.view.PostEngagementView(
                        POST_ID, Map.of(ReactionType.LIKE, 2L, ReactionType.LOVE, 1L), 3, 8, ReactionType.LOVE, 0, 0, 0));

        var summary = new GetPostReactionSummaryUseCaseImpl(
                postSupport, currentUserProvider, metricsRepository).execute(POST_ID);

        assertThat(summary.totalCount()).isEqualTo(3);
        assertThat(summary.counts()).hasSize(ReactionType.values().length);
        assertThat(summary.counts().get(ReactionType.LIKE)).isEqualTo(2);
        assertThat(summary.counts().get(ReactionType.ANGRY)).isZero();
        assertThat(summary.currentUserReaction()).isEqualTo(ReactionType.LOVE);
    }

    @Test
    void removingMissingReactionRemainsIdempotent() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(ACTOR_ID);

        new RemovePostReactionUseCaseImpl(repository, postSupport, currentUserProvider, engagement)
                .execute(POST_ID);

        verify(repository).deleteByPostIdAndActorId(POST_ID, ACTOR_ID);
    }
}
