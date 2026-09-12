package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostCommentEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPostCommentRepository extends JpaRepository<JpaPostCommentEntity, UUID> {
    Optional<JpaPostCommentEntity> findByIdAndPostIdAndDeletedAtIsNull(UUID id, UUID postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from JpaPostCommentEntity c
            where c.id = :id and c.postId = :postId and c.deletedAt is null
            """)
    Optional<JpaPostCommentEntity> findByIdAndPostIdForUpdate(
            @Param("id") UUID id,
            @Param("postId") UUID postId
    );

    @Query("""
            select c from JpaPostCommentEntity c
            where c.postId = :postId
              and c.parentCommentId is null
              and c.status = 'PUBLISHED'
              and c.deletedAt is null
            """)
    Page<JpaPostCommentEntity> findPublishedRoots(
            @Param("postId") UUID postId,
            Pageable pageable
    );

    @Query("""
            select c from JpaPostCommentEntity c
            where c.postId = :postId
              and c.parentCommentId in :parentIds
              and c.status = 'PUBLISHED'
              and c.deletedAt is null
            order by c.createdAt asc, c.id asc
            """)
    List<JpaPostCommentEntity> findPublishedReplies(
            @Param("postId") UUID postId,
            @Param("parentIds") List<UUID> parentIds
    );
}
