package com.nongthinh.location_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.location_service.domain.commune.Commune;

public interface CommuneRepository {

    boolean existsById(UUID id);

    boolean existsByCode(String code);

    boolean existsByProvinceId(String provinceId);

    Optional<Commune> findById(UUID id);

    List<Commune> findByProvinceIdOrderByCode(String provinceId);

    Commune save(Commune commune);

    void deleteById(UUID id);
}
