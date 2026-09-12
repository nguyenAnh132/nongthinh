package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostShareEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostShareRepository extends JpaRepository<JpaPostShareEntity, UUID> {
    long countByPostId(UUID postId);
}
