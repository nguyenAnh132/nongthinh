package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostMediaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostMediaRepository extends JpaRepository<JpaPostMediaEntity, UUID> {
    List<JpaPostMediaEntity> findAllByPostIdOrderByDisplayOrderAsc(UUID postId);
    List<JpaPostMediaEntity> findAllByPostIdInOrderByPostIdAscDisplayOrderAsc(List<UUID> postIds);
    void deleteAllByPostId(UUID postId);
}
