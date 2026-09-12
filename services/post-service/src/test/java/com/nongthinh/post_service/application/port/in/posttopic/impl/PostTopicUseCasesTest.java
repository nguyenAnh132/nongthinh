package com.nongthinh.post_service.application.port.in.posttopic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.CreatePostTopicCommand;
import com.nongthinh.post_service.application.model.TrendingPostTopic;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.TrendingPostTopicQuery;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostTopic;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostTopicUseCasesTest {
    private static final UUID ID = UUID.fromString("0cb18a40-2702-4f62-ad62-d30b80fb0e11");
    private static final Instant NOW = Instant.parse("2026-08-22T10:00:00Z");

    @Mock private PostTopicRepository repository;
    @Mock private TrendingPostTopicQuery trendingPostTopicQuery;
    @Mock private PostRepository postRepository;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    private PostTopicUseCaseSupport support;

    @BeforeEach
    void setUp() {
        support = new PostTopicUseCaseSupport(repository);
    }

    @Test
    void createsTopicWithImmutableSlug() {
        when(idGenerator.generate()).thenReturn(ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostTopicUseCaseImpl(repository, idGenerator, clockProvider)
                .execute(new CreatePostTopicCommand("Sâu bệnh", "sau-benh", null, 10));

        assertEquals(ID, view.id());
        assertEquals("sau-benh", view.slug());
    }

    @Test
    void rejectsDuplicatedSlug() {
        when(repository.findBySlug("sau-benh")).thenReturn(Optional.of(topic()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new CreatePostTopicUseCaseImpl(repository, idGenerator, clockProvider)
                        .execute(new CreatePostTopicCommand("Sâu bệnh", "sau-benh", null, 10)));

        assertEquals(ErrorCode.POST_TOPIC_SLUG_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void preventsDeletingTopicUsedByPosts() {
        when(repository.findById(ID)).thenReturn(Optional.of(topic()));
        when(postRepository.existsByTopicId(ID)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new DeletePostTopicUseCaseImpl(support, repository, postRepository).execute(ID));

        assertEquals(ErrorCode.POST_TOPIC_IN_USE, exception.getErrorCode());
        verify(repository, never()).deleteById(ID);
    }

    @Test
    void ranksTrendingTopicsInRepositoryOrder() {
        UUID secondId = UUID.fromString("8c37cce0-5a7b-4515-ad64-2e48a1df83a4");
        when(trendingPostTopicQuery.findTop(3)).thenReturn(List.of(
                new TrendingPostTopic(ID, "Rice diseases", "rice-diseases", 12),
                new TrendingPostTopic(secondId, "Coffee prices", "coffee-prices", 8)
        ));

        var views = new ListTrendingPostTopicsUseCaseImpl(trendingPostTopicQuery).execute(3);

        assertEquals(1, views.get(0).rank());
        assertEquals(12, views.get(0).postCount());
        assertEquals(2, views.get(1).rank());
        assertEquals(secondId, views.get(1).id());
    }

    @Test
    void rejectsTrendingTopicLimitOutsideSupportedRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListTrendingPostTopicsUseCaseImpl(trendingPostTopicQuery).execute(0));
        verify(trendingPostTopicQuery, never()).findTop(any(Integer.class));
    }

    private PostTopic topic() {
        return PostTopic.create(ID, "Sâu bệnh", "sau-benh", null, 10, NOW);
    }
}
