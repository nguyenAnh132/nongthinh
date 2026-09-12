package com.nongthinh.post_service.application.port.in.postreaction.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.model.ReactionPage;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListPostReactionsUseCaseImplTest {
    private static final UUID POST_ID = UUID.fromString("78bab3eb-6613-42c3-9d95-e8be298ef242");
    private static final UUID ACTOR_ID = UUID.fromString("7c20c104-8a6d-408d-9f76-d86b5e8e5e1c");
    @Mock private ReactionRepository repository;
    @Mock private PostUseCaseSupport postSupport;
    @InjectMocks private ListPostReactionsUseCaseImpl useCase;

    @Test
    void returnsActorsAndReactionTypesWithRepositoryPagination() {
        var now = Instant.parse("2026-09-12T00:00:00Z");
        var reaction = Reaction.reconstruct(ACTOR_ID, POST_ID, ACTOR_ID, ReactionType.LOVE, now, now);
        when(repository.findByPostId(POST_ID, ReactionType.LOVE, 1, 10))
                .thenReturn(new ReactionPage(List.of(reaction), 1, 10, 11, 2, false));

        var result = useCase.execute(POST_ID, ReactionType.LOVE, 1, 10);

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.actorId()).isEqualTo(ACTOR_ID);
            assertThat(item.reactionType()).isEqualTo(ReactionType.LOVE);
        });
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(11);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
        verify(postSupport).requireInteractablePost(POST_ID);
        verify(postSupport).validatePage(1, 10);
    }

    @Test
    void doesNotListActorsOfUnavailablePosts() {
        when(postSupport.requireInteractablePost(POST_ID))
                .thenThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));
        assertThatThrownBy(() -> useCase.execute(POST_ID, null, 0, 10)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidPaginationBeforeQuerying() {
        doThrow(new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER))
                .when(postSupport).validatePage(-1, 10);
        assertThatThrownBy(() -> useCase.execute(POST_ID, null, -1, 10)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(repository);
    }
}
