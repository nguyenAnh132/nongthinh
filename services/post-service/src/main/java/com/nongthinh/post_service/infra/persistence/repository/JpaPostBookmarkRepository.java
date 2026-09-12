package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostBookmarkRepository extends JpaRepository<JpaPostBookmarkEntity, JpaPostBookmarkId> {
}
