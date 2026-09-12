package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostReportEntity;
import jakarta.persistence.LockModeType;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPostReportRepository extends JpaRepository<JpaPostReportEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from JpaPostReportEntity r where r.id = :id")
    Optional<JpaPostReportEntity> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);

    Page<JpaPostReportEntity> findAllByStatus(String status, Pageable pageable);
}
