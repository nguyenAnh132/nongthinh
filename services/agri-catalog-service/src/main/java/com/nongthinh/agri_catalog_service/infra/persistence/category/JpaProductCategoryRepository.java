package com.nongthinh.agri_catalog_service.infra.persistence.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductCategoryRepository extends JpaRepository<JpaProductCategoryEntity, UUID> {

    boolean existsBySlugIgnoreCaseAndDeletedAtIsNull(String slug);

    Optional<JpaProductCategoryEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<JpaProductCategoryEntity> findBySlugIgnoreCaseAndDeletedAtIsNull(String slug);

    List<JpaProductCategoryEntity> findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();

    List<JpaProductCategoryEntity> findAllByDeletedAtIsNullAndIsActiveTrueOrderByDisplayOrderAscNameAsc();

    List<JpaProductCategoryEntity> findAllByParentIdAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(
            UUID parentId
    );
}
