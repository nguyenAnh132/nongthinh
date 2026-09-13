package com.nongthinh.post_service.infra.persistence.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.infra.persistence.mapper.PostPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostCropTypeRepository;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostMediaRepository;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostRepository;
import java.util.UUID;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PostRepositoryImplTest {
    @Mock private JpaPostRepository posts;
    @Mock private JpaPostMediaRepository media;
    @Mock private JpaPostCropTypeRepository cropTypes;
    @Mock private PostPersistenceMapper mapper;
    private PostRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PostRepositoryImpl(
                posts, media, cropTypes, mapper,
                new PostLimitsProperties(2_000, 10, 100));
    }

    @Test
    void usesQueryWithoutTextFunctionsWhenKeywordIsNull() {
        when(posts.findPublic(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findPublic(null, null, null, null, null, 0, 20);

        verify(posts).findPublic(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
        verify(posts, never()).findPublicByKeyword(
                any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void usesSearchQueryWhenKeywordIsPresent() {
        UUID postTypeId = UUID.randomUUID();
        when(posts.findPublicByKeyword(
                eq(postTypeId), isNull(), isNull(), isNull(), eq("lúa"),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findPublic(postTypeId, null, null, null, "lúa", 0, 20);

        verify(posts).findPublicByKeyword(
                eq(postTypeId), isNull(), isNull(), isNull(), eq("lúa"),
                any(Pageable.class));
        verify(posts, never()).findPublic(
                eq(postTypeId), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void passesAuthorFilterToThePublicPostQuery() {
        UUID authorId = UUID.randomUUID();
        when(posts.findPublic(isNull(), isNull(), isNull(), eq(authorId), any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findPublic(null, null, null, authorId, null, 0, 20);

        verify(posts).findPublic(isNull(), isNull(), isNull(), eq(authorId), any(Pageable.class));
    }

    @Test
    void appliesAllAdministratorPostFiltersInOneQuery() {
        UUID authorId = UUID.randomUUID();
        Instant from = Instant.parse("2026-08-21T00:00:00Z");
        Instant to = Instant.parse("2026-08-28T00:00:00Z");
        when(posts.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findAllForAdmin(
                authorId,
                com.nongthinh.post_service.domain.post.valueobject.PostStatus.HIDDEN,
                "sâu bệnh",
                from,
                to,
                0,
                20
        );

        verify(posts).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void avoidsBindingNullAdministratorFilters() {
        when(posts.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findAllForAdmin(null, null, null, null, null, 0, 20);

        verify(posts).findAll(any(Specification.class), any(Pageable.class));
    }
}
