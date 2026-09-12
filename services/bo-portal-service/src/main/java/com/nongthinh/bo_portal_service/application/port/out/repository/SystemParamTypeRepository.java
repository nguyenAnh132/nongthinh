package com.nongthinh.bo_portal_service.application.port.out.repository;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.List;
import java.util.Optional;

public interface SystemParamTypeRepository {

    List<SystemParamType> findAll();

    Optional<SystemParamType> findById(Long id);

    Optional<SystemParamType> findByName(String name);

    boolean existsByName(String name);

    SystemParamType save(SystemParamType systemParamType);
}
