package com.nongthinh.agri_catalog_service.infra.persistence.product_image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductImageRepository extends JpaRepository<JpaProductImageEntity, UUID> {

    Optional<JpaProductImageEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<JpaProductImageEntity> findByFileIdAndDeletedAtIsNull(UUID fileId);

    Optional<JpaProductImageEntity> findByProductIdAndIsPrimaryTrueAndDeletedAtIsNull(UUID productId);

    List<JpaProductImageEntity> findAllByProductIdAndDeletedAtIsNullOrderByDisplayOrderAscCreatedAtAsc(
            UUID productId
    );
}
