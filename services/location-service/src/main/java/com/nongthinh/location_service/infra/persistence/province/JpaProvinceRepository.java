package com.nongthinh.location_service.infra.persistence.province;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProvinceRepository extends JpaRepository<JpaProvinceEntity, String> {

    boolean existsByCode(String code);

    List<JpaProvinceEntity> findAllByOrderByCodeAsc();
}
