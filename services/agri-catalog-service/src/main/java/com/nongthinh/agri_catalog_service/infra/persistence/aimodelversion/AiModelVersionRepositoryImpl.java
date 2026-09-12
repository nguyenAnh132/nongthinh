package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiModelVersionRepositoryImpl implements AiModelVersionRepository {

    private final JpaAiModelVersionRepository jpaAiModelVersionRepository;
    private final JpaAiModelVersionClassRepository jpaAiModelVersionClassRepository;
    private final AiModelVersionPersistenceMapper mapper;

    @Override
    public boolean existsByModelIdAndVersion(UUID modelId, String version) {
        return jpaAiModelVersionRepository.existsByModelIdAndVersion(modelId, version == null ? null : version.trim());
    }

    @Override
    public boolean existsByModelId(UUID modelId) {
        return jpaAiModelVersionRepository.existsByModelId(modelId);
    }

    @Override
    public Optional<AiModelVersion> findById(UUID id) {
        return jpaAiModelVersionRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<AiModelVersionClass> findClassById(UUID id) {
        return jpaAiModelVersionClassRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<AiModelVersion> findAllByModelId(UUID modelId) {
        return jpaAiModelVersionRepository.findAllByModelIdOrderByCreatedAtDesc(modelId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public AiModelVersion save(AiModelVersion version) {
        boolean isNewVersion = !jpaAiModelVersionRepository.existsById(version.getId());
        JpaAiModelVersionEntity saved = jpaAiModelVersionRepository.save(mapper.toEntity(version));
        if (isNewVersion) {
            jpaAiModelVersionClassRepository.saveAll(version.getClasses().stream().map(mapper::toEntity).toList());
        }
        return toDomain(saved);
    }

    private AiModelVersion toDomain(JpaAiModelVersionEntity entity) {
        return mapper.toDomain(
                entity,
                jpaAiModelVersionClassRepository.findAllByModelVersionIdOrderByClassIndexAsc(entity.getId()));
    }
}
