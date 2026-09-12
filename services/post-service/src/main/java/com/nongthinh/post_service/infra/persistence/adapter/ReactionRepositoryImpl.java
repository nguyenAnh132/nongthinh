package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.application.model.ReactionPage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import com.nongthinh.post_service.infra.persistence.mapper.InteractionPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostReactionRepository;
import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReactionRepositoryImpl implements ReactionRepository {
    private final JpaPostReactionRepository repository;
    private final InteractionPersistenceMapper mapper;
    private final JdbcTemplate jdbc;

    public ReactionRepositoryImpl(JpaPostReactionRepository repository,
                                  InteractionPersistenceMapper mapper,
                                  JdbcTemplate jdbc) {
        this.repository = repository;
        this.mapper = mapper;
        this.jdbc = jdbc;
    }
    public Optional<Reaction> findByPostIdAndActorId(UUID postId, UUID actorId) { return repository.findByPostIdAndActorId(postId, actorId).map(mapper::toDomain); }

    @Override
    public ReactionPage findByPostId(UUID postId, ReactionType reactionType, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var result = reactionType == null
                ? repository.findByPostId(postId, pageable)
                : repository.findByPostIdAndReactionType(postId, reactionType.name(), pageable);
        return new ReactionPage(result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.hasNext());
    }

    public Map<ReactionType, Long> countByPostId(UUID postId) {
        EnumMap<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (Object[] row : repository.countByPostIdGroupedByType(postId)) {
            counts.put(ReactionType.valueOf((String) row[0]), (Long) row[1]);
        }
        return Map.copyOf(counts);
    }

    public Reaction upsert(Reaction value) {
        return jdbc.queryForObject("""
                        INSERT INTO post_reactions (
                            id, post_id, actor_id, reaction_type, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        ON CONFLICT (post_id, actor_id) DO UPDATE
                        SET reaction_type = EXCLUDED.reaction_type,
                            updated_at = GREATEST(post_reactions.updated_at, EXCLUDED.updated_at)
                        RETURNING id, post_id, actor_id, reaction_type, created_at, updated_at
                        """,
                (resultSet, rowNumber) -> Reaction.reconstruct(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getObject("post_id", UUID.class),
                        resultSet.getObject("actor_id", UUID.class),
                        ReactionType.valueOf(resultSet.getString("reaction_type")),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                ),
                value.getId(), value.getPostId(), value.getActorId(), value.getType().name(),
                Timestamp.from(value.getCreatedAt()), Timestamp.from(value.getUpdatedAt()));
    }

    public void deleteByPostIdAndActorId(UUID postId, UUID actorId) { repository.deleteByPostIdAndActorId(postId, actorId); }
}
