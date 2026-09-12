package com.nongthinh.post_service.infra.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostReactionEntity;
import com.nongthinh.post_service.infra.persistence.mapper.InteractionPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostReactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ReactionRepositoryImplTest {
    @Mock private JpaPostReactionRepository repository;
    @Mock private JdbcTemplate jdbc;

    @Test
    void pagesAtDatabaseWithStableCreationTimeAndIdOrdering() {
        var postId = UUID.fromString("78bab3eb-6613-42c3-9d95-e8be298ef242");
        var actorId = UUID.fromString("7c20c104-8a6d-408d-9f76-d86b5e8e5e1c");
        var now = Instant.parse("2026-09-12T00:00:00Z");
        var entity = new JpaPostReactionEntity(actorId, postId, actorId, "LOVE", now, now);
        var pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        when(repository.findByPostId(postId, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 11));

        var adapter = new ReactionRepositoryImpl(repository, Mappers.getMapper(InteractionPersistenceMapper.class), jdbc);
        var result = adapter.findByPostId(postId, null, 1, 10);

        assertThat(result.items()).singleElement().satisfies(item -> assertThat(item.getActorId()).isEqualTo(actorId));
        assertThat(result.totalElements()).isEqualTo(11);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void filtersByReactionTypeAtDatabase() {
        var postId = UUID.fromString("78bab3eb-6613-42c3-9d95-e8be298ef242");
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        when(repository.findByPostIdAndReactionType(postId, "LOVE", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        var adapter = new ReactionRepositoryImpl(
                repository, Mappers.getMapper(InteractionPersistenceMapper.class), jdbc);

        adapter.findByPostId(
                postId, com.nongthinh.post_service.domain.interaction.valueobject.ReactionType.LOVE, 0, 10);

        verify(repository).findByPostIdAndReactionType(postId, "LOVE", pageable);
    }
}
