package com.nongthinh.post_service.application.port.in.posthistory.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.model.PostHistoryFilter;
import com.nongthinh.post_service.application.model.PostHistoryPage;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.application.service.PostHistoryUseCaseSupport;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostHistoryUseCasesTest {
    private static final UUID HISTORY_ID = UUID.fromString("2fe06a60-d43d-4e9c-8587-ee055b016920");
    private static final UUID POST_ID = UUID.fromString("03df433b-cdf9-40bc-aa99-e5b54e4f30ba");
    private static final UUID POST_TYPE_ID = UUID.fromString("7b37a0c6-d631-4c6f-8434-f8b5f31a2831");
    private static final UUID AUTHOR_ID = UUID.fromString("d9205809-0a9b-45fa-8990-e18ec86e2ed1");
    private static final UUID ACTOR_ID = UUID.fromString("73c0715d-43fc-4c61-a73a-463706e5b353");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Mock private PostHistoryRepository repository;
    private PostHistoryUseCaseSupport support;

    @BeforeEach
    void setUp() {
        support = new PostHistoryUseCaseSupport(
                repository,
                new PostLimitsProperties(2_000, 10, 100)
        );
    }

    @Test
    void administratorCanFilterAcrossAuthorsActorsAndActions() {
        PostHistory history = history();
        PostHistoryFilter expectedFilter = new PostHistoryFilter(
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.HIDDEN
        );
        when(repository.findAll(expectedFilter, 2, 10))
                .thenReturn(new PostHistoryPage(List.of(history), 2, 10, 21, 3, false));

        var page = new ListPostHistoriesUseCaseImpl(repository, support).execute(
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.HIDDEN,
                2,
                10
        );

        assertThat(page.page()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(21);
        assertThat(page.items()).hasSize(1);
        verify(repository).findAll(expectedFilter, 2, 10);
    }

    @Test
    void rejectsPaginationAboveConfiguredLimitBeforeRepositoryQuery() {
        assertThatThrownBy(() -> new ListPostHistoriesUseCaseImpl(repository, support).execute(
                null,
                null,
                null,
                null,
                null,
                0,
                101
        )).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_PARAMETER));

        ArgumentCaptor<PostHistoryFilter> captor = ArgumentCaptor.forClass(PostHistoryFilter.class);
        verify(repository, org.mockito.Mockito.never()).findAll(captor.capture(),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    private static PostHistory history() {
        PostSnapshot snapshot = new PostSnapshot(
                "Updated rice advice",
                POST_TYPE_ID,
                null,
                Set.of(),
                "Đồng Tháp",
                List.of(),
                PostStatus.PUBLISHED,
                PostVisibility.PUBLIC
        );
        return PostHistory.create(
                HISTORY_ID,
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.UPDATED,
                PostStatus.PUBLISHED,
                PostStatus.PUBLISHED,
                PostVisibility.PUBLIC,
                PostVisibility.PUBLIC,
                null,
                null,
                null,
                snapshot,
                NOW
        );
    }
}
