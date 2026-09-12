package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SystemParamTypeRepositoryImpl implements SystemParamTypeRepository {

    private final JpaSystemParamTypeRepository jpaRepository;
    private final SystemParamTypePersistenceMapper mapper;

    @Override
    public List<SystemParamType> findAll() {
        return jpaRepository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SystemParamType> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SystemParamType> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public SystemParamType save(SystemParamType systemParamType) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(systemParamType)));
    }
}
