package com.nongthinh.bo_portal_service.application.port.out.repository;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import java.util.List;
import java.util.Optional;

public interface SystemParamRepository {

    List<SystemParam> findAll();

    List<SystemParam> findAllByTypeId(Long typeId);

    Optional<SystemParam> findByName(String name);

    boolean existsByName(String name);

    SystemParam save(SystemParam systemParam);

    void deleteByName(String name);
}
