package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SystemParamRepositoryImpl implements SystemParamRepository {

    private final JpaSystemParamRepository jpaRepository;
    private final SystemParamPersistenceMapper mapper;

    @Override
    public List<SystemParam> findAll() {
        return jpaRepository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SystemParam> findAllByTypeId(Long typeId) {
        return jpaRepository.findAllByTypeIdOrderByNameAsc(typeId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SystemParam> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public SystemParam save(SystemParam systemParam) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(systemParam)));
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        jpaRepository.deleteByName(name);
    }
}
