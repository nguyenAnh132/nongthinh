package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;

public interface CropTypeRepository {

    boolean existsByCode(String code);

    Optional<CropType> findById(UUID id);

    List<CropType> findAll();

    List<CropType> findAllActive();

    CropType save(CropType cropType);
}

