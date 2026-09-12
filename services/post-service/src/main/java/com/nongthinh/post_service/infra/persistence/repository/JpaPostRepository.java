package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPostRepository extends JpaRepository<JpaPostEntity, UUID>,
        JpaSpecificationExecutor<JpaPostEntity> {
    Optional<JpaPostEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from JpaPostEntity p where p.id = :id and p.deletedAt is null")
    Optional<JpaPostEntity> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from JpaPostEntity p where p.id = :id")
    Optional<JpaPostEntity> findByIdForUpdateIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select p from JpaPostEntity p
            where p.deletedAt is null
              and p.status = 'PUBLISHED'
              and p.visibility = 'PUBLIC'
              and (:postTypeId is null or p.postTypeId = :postTypeId)
              and (:topicId is null or p.topicId = :topicId)
              and (:cropTypeId is null or exists (
                    select pc from JpaPostCropTypeEntity pc
                    where pc.id.postId = p.id and pc.id.cropTypeId = :cropTypeId
              ))
            """)
    Page<JpaPostEntity> findPublic(
            @Param("postTypeId") UUID postTypeId,
            @Param("topicId") UUID topicId,
            @Param("cropTypeId") UUID cropTypeId,
            Pageable pageable
    );

    @Query("""
            select p from JpaPostEntity p
            where p.deletedAt is null
              and p.status = 'PUBLISHED'
              and p.visibility = 'PUBLIC'
              and (:postTypeId is null or p.postTypeId = :postTypeId)
              and (:topicId is null or p.topicId = :topicId)
              and (:cropTypeId is null or exists (
                    select pc from JpaPostCropTypeEntity pc
                    where pc.id.postId = p.id and pc.id.cropTypeId = :cropTypeId
              ))
              and lower(p.content) like lower(concat('%', :keyword, '%'))
            """)
    Page<JpaPostEntity> findPublicByKeyword(
            @Param("postTypeId") UUID postTypeId,
            @Param("topicId") UUID topicId,
            @Param("cropTypeId") UUID cropTypeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select p from JpaPostEntity p
            where p.deletedAt is null
              and p.authorUserId = :authorUserId
              and (:status is null or p.status = :status)
            """)
    Page<JpaPostEntity> findByAuthor(
            @Param("authorUserId") UUID authorUserId,
            @Param("status") String status,
            Pageable pageable
    );

    long countByDeletedAtIsNull();

    @Query("""
            select p.status, count(p)
            from JpaPostEntity p
            where p.deletedAt is null
            group by p.status
            """)
    List<Object[]> countActivePostsGroupedByStatus();

    @Query("""
            select p.createdAt
            from JpaPostEntity p
            where p.deletedAt is null
              and p.createdAt >= :from
              and p.createdAt < :to
            order by p.createdAt asc
            """)
    List<Instant> findCreatedAtBetween(
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query(value = """
            select p
            from JpaPostEntity p, JpaPostBookmarkEntity b
            where b.id.postId = p.id
              and b.id.userId = :userId
              and p.deletedAt is null
              and p.status = 'PUBLISHED'
              and p.visibility = 'PUBLIC'
            order by b.createdAt desc, p.id desc
            """, countQuery = """
            select count(p)
            from JpaPostEntity p, JpaPostBookmarkEntity b
            where b.id.postId = p.id
              and b.id.userId = :userId
              and p.deletedAt is null
              and p.status = 'PUBLISHED'
              and p.visibility = 'PUBLIC'
            """)
    Page<JpaPostEntity> findBookmarkedByUser(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    boolean existsByPostTypeId(UUID postTypeId);
    boolean existsByTopicId(UUID topicId);
}
