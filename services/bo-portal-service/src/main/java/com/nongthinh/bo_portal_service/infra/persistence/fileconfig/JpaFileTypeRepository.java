package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFileTypeRepository extends JpaRepository<JpaFileTypeEntity, String> {
    List<JpaFileTypeEntity> findAllByOrderByCodeAsc();
}
