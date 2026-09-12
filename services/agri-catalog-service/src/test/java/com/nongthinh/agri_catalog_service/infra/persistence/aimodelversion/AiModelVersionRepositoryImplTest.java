package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;

@ExtendWith(MockitoExtension.class)
class AiModelVersionRepositoryImplTest {

    private static final UUID VERSION_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID MODEL_ID = UUID.fromString("a0000000-0000-0000-0000-000000000002");
    private static final UUID ARTIFACT_FILE_ID = UUID.fromString("a0000000-0000-0000-0000-000000000003");
    private static final UUID CLASS_ID = UUID.fromString("a0000000-0000-0000-0000-000000000004");
    private static final UUID ADMIN_ID = UUID.fromString("a0000000-0000-0000-0000-000000000005");
    private static final Instant NOW = Instant.parse("2026-08-13T00:00:00Z");

    @Mock
    private JpaAiModelVersionRepository versionJpaRepository;

    @Mock
    private JpaAiModelVersionClassRepository classJpaRepository;

    @Test
    void updatingExistingVersionPreservesImmutableManifestRows() {
        AiModelVersion version = version();
        version.startValidation();
        version.completeValidation(true, "{\"status\":\"VALIDATED\"}", ADMIN_ID, NOW);
        stubSavedVersion(version);
        when(versionJpaRepository.existsById(VERSION_ID)).thenReturn(true);

        AiModelVersion saved = repository().save(version);

        assertEquals("VALIDATED", saved.getStatus().name());
        verify(classJpaRepository, never()).saveAll(any());
    }

    @Test
    void creatingVersionPersistsItsManifestRows() {
        AiModelVersion version = version();
        stubSavedVersion(version);
        when(versionJpaRepository.existsById(VERSION_ID)).thenReturn(false);

        repository().save(version);

        verify(classJpaRepository).saveAll(any());
    }

    private AiModelVersionRepositoryImpl repository() {
        return new AiModelVersionRepositoryImpl(
                versionJpaRepository, classJpaRepository, new AiModelVersionPersistenceMapper());
    }

    private void stubSavedVersion(AiModelVersion version) {
        AiModelVersionPersistenceMapper mapper = new AiModelVersionPersistenceMapper();
        when(versionJpaRepository.save(any(JpaAiModelVersionEntity.class))).thenReturn(mapper.toEntity(version));
        when(classJpaRepository.findAllByModelVersionIdOrderByClassIndexAsc(VERSION_ID))
                .thenReturn(List.of(mapper.toEntity(version.getClasses().getFirst())));
    }

    private AiModelVersion version() {
        return AiModelVersion.create(
                VERSION_ID,
                MODEL_ID,
                "1.0.0",
                ARTIFACT_FILE_ID,
                "a".repeat(64),
                640,
                640,
                List.of(AiModelVersionClass.create(
                        CLASS_ID,
                        VERSION_ID,
                        0,
                        "BROWN_SPOT",
                        "Brown spot",
                        AiModelClassKind.DISEASE)),
                ADMIN_ID,
                NOW);
    }
}
