package com.nongthinh.agri_catalog_service.infra.persistence.producthistory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductHistoryRepository
        extends JpaRepository<JpaProductHistoryEntity, UUID> {

    List<JpaProductHistoryEntity> findAllByProductIdOrderByCreatedAtDesc(UUID productId);
}
