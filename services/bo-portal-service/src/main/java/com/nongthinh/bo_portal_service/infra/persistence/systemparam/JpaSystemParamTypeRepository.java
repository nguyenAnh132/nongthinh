package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSystemParamTypeRepository extends JpaRepository<JpaSystemParamTypeEntity, Long> {

    List<JpaSystemParamTypeEntity> findAllByOrderByNameAsc();

    Optional<JpaSystemParamTypeEntity> findByName(String name);

    boolean existsByName(String name);
}
