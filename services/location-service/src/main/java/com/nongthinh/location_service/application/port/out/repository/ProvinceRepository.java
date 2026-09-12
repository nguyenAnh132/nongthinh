package com.nongthinh.location_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import com.nongthinh.location_service.domain.province.Province;

public interface ProvinceRepository {

    boolean existsById(String id);

    boolean existsByCode(String code);

    Optional<Province> findById(String id);

    List<Province> findAllOrderByCode();

    Province save(Province province);

    void deleteById(String id);
}
