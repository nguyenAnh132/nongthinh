package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;

public interface AiModelRepository {

    boolean existsByCode(String code);

    boolean existsByCropTypeId(UUID cropTypeId);

    Optional<AiModel> findById(UUID id);

    List<AiModel> findAll();

    AiModel save(AiModel aiModel);
}
