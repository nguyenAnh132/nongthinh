package com.nongthinh.location_service.infra.persistence.province;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.domain.province.Province;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProvinceRepositoryImpl implements ProvinceRepository {

    private final JpaProvinceRepository jpaProvinceRepository;
    private final ProvincePersistenceMapper provincePersistenceMapper;

    @Override
    public boolean existsById(String id) {
        return jpaProvinceRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaProvinceRepository.existsByCode(code);
    }

    @Override
    public Optional<Province> findById(String id) {
        return jpaProvinceRepository.findById(id)
                .map(provincePersistenceMapper::toDomain);
    }

    @Override
    public List<Province> findAllOrderByCode() {
        return jpaProvinceRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(provincePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Province save(Province province) {
        JpaProvinceEntity entity = provincePersistenceMapper.toEntity(province);
        JpaProvinceEntity saved = jpaProvinceRepository.save(entity);
        return provincePersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        jpaProvinceRepository.deleteById(id);
    }
}
