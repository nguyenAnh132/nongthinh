package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSystemParamRepository extends JpaRepository<JpaSystemParamEntity, Long> {

    Optional<JpaSystemParamEntity> findByName(String name);

    boolean existsByName(String name);

    List<JpaSystemParamEntity> findAllByOrderByNameAsc();

    List<JpaSystemParamEntity> findAllByTypeIdOrderByNameAsc(Long typeId);

    void deleteByName(String name);
}
