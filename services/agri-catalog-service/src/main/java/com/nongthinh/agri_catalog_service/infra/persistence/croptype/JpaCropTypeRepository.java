package com.nongthinh.agri_catalog_service.infra.persistence.croptype;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCropTypeRepository extends JpaRepository<JpaCropTypeEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<JpaCropTypeEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<JpaCropTypeEntity> findAllByDeletedAtIsNullOrderByNameAsc();

    List<JpaCropTypeEntity> findAllByDeletedAtIsNullAndActiveTrueOrderByNameAsc();
}

