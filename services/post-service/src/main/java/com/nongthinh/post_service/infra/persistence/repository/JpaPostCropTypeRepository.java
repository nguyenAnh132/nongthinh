package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostCropTypeEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCropTypeId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostCropTypeRepository extends JpaRepository<JpaPostCropTypeEntity, JpaPostCropTypeId> {
    List<JpaPostCropTypeEntity> findAllByIdPostId(UUID postId);
    List<JpaPostCropTypeEntity> findAllByIdPostIdIn(List<UUID> postIds);
}
