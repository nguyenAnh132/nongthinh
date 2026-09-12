package com.nongthinh.location_service.infra.persistence.commune;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCommuneRepository extends JpaRepository<JpaCommuneEntity, UUID> {

    boolean existsByCode(String code);

    boolean existsByProvinceId(String provinceId);

    List<JpaCommuneEntity> findByProvinceIdOrderByCodeAsc(String provinceId);
}
