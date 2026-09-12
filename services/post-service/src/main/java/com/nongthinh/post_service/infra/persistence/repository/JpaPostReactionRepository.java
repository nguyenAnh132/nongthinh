package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostReactionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPostReactionRepository extends JpaRepository<JpaPostReactionEntity, UUID> {
    Page<JpaPostReactionEntity> findByPostId(UUID postId, Pageable pageable);
    Page<JpaPostReactionEntity> findByPostIdAndReactionType(
            UUID postId, String reactionType, Pageable pageable);

    Optional<JpaPostReactionEntity> findByPostIdAndActorId(UUID postId, UUID actorId);

    @Query("""
            select r.reactionType, count(r)
            from JpaPostReactionEntity r
            where r.postId = :postId
            group by r.reactionType
            """)
    List<Object[]> countByPostIdGroupedByType(@Param("postId") UUID postId);

    void deleteByPostIdAndActorId(UUID postId, UUID actorId);
}
