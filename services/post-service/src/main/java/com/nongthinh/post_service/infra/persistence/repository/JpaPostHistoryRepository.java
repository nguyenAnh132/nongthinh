package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostHistoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaPostHistoryRepository extends JpaRepository<JpaPostHistoryEntity, UUID>,
        JpaSpecificationExecutor<JpaPostHistoryEntity> {
}
