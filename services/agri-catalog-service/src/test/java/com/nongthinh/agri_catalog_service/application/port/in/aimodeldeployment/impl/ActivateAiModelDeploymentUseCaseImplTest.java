package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl.AiModelVersionUseCaseSupport;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelRuntimeWarmer;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionMappingReadiness;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentStatus;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;

class ActivateAiModelDeploymentUseCaseImplTest {

    @Test
    void atomicallyReplacesTheActiveDeploymentInTheSameRoutingSlot() {
        Instant now = Instant.parse("2026-08-19T10:00:00Z");
        UUID actorId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID cropTypeId = UUID.randomUUID();
        AiModelVersion previousVersion = version(modelId, "v2", AiModelVersionStatus.ACTIVE, now);
        AiModelVersion rollbackVersion = version(modelId, "v1", AiModelVersionStatus.READY, now);
        AiModelDeployment activeDeployment = deployment(previousVersion.getId(), cropTypeId, AiModelDeploymentStatus.ACTIVE, now);
        AiModelDeployment rollbackDeployment = deployment(rollbackVersion.getId(), cropTypeId, AiModelDeploymentStatus.INACTIVE, now);

        AiModelVersionUseCaseSupport support = mock(AiModelVersionUseCaseSupport.class);
        AiModelDeploymentRepository deploymentRepository = mock(AiModelDeploymentRepository.class);
        AiModelVersionRepository versionRepository = mock(AiModelVersionRepository.class);
        AiModelRuntimeWarmer runtimeWarmer = mock(AiModelRuntimeWarmer.class);
        AiModelVersionMappingReadiness mappingReadiness = mock(AiModelVersionMappingReadiness.class);
        ClockProvider clockProvider = mock(ClockProvider.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        AiModel model = model(modelId, cropTypeId, actorId, now);

        when(deploymentRepository.findById(rollbackDeployment.getId())).thenReturn(Optional.of(rollbackDeployment));
        when(deploymentRepository.findActiveWithScopeCropAndPriority(
                AiModelDeploymentScope.CROP, cropTypeId, 0, rollbackDeployment.getId()))
                .thenReturn(Optional.of(activeDeployment));
        when(deploymentRepository.existsActiveByModelVersionId(previousVersion.getId())).thenReturn(false);
        when(deploymentRepository.saveAndFlush(any(AiModelDeployment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(support.requireVersion(rollbackVersion.getId())).thenReturn(rollbackVersion);
        when(support.requireVersion(previousVersion.getId())).thenReturn(previousVersion);
        when(support.requireModel(modelId)).thenReturn(model);
        when(mappingReadiness.hasCompleteDiseaseMappings(rollbackVersion)).thenReturn(true);
        when(clockProvider.now()).thenReturn(now.plusSeconds(60));
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                actorId, "keycloak", "admin@example.com", Set.of("ROLE_ADMIN"), null, Set.of()));

        var useCase = new ActivateAiModelDeploymentUseCaseImpl(
                support,
                deploymentRepository,
                versionRepository,
                runtimeWarmer,
                mappingReadiness,
                clockProvider,
                currentUserProvider);

        var result = useCase.execute(rollbackDeployment.getId());

        assertThat(result.status()).isEqualTo(AiModelDeploymentStatus.ACTIVE);
        assertThat(activeDeployment.getStatus()).isEqualTo(AiModelDeploymentStatus.INACTIVE);
        assertThat(previousVersion.getStatus()).isEqualTo(AiModelVersionStatus.READY);
        assertThat(rollbackVersion.getStatus()).isEqualTo(AiModelVersionStatus.ACTIVE);
        verify(runtimeWarmer).warm(any());
        verify(deploymentRepository).saveAndFlush(activeDeployment);
        verify(deploymentRepository).saveAndFlush(rollbackDeployment);
        verify(versionRepository).save(previousVersion);
        verify(versionRepository).save(rollbackVersion);
    }

    private AiModelDeployment deployment(
            UUID versionId, UUID cropTypeId, AiModelDeploymentStatus status, Instant now) {
        return AiModelDeployment.reconstruct(
                UUID.randomUUID(), versionId, cropTypeId, AiModelDeploymentScope.CROP, 0,
                status, now, UUID.randomUUID(), status == AiModelDeploymentStatus.ACTIVE ? now : null,
                status == AiModelDeploymentStatus.ACTIVE ? UUID.randomUUID() : null);
    }

    private AiModelVersion version(UUID modelId, String name, AiModelVersionStatus status, Instant now) {
        UUID versionId = UUID.randomUUID();
        return AiModelVersion.reconstruct(
                versionId,
                modelId,
                name,
                UUID.randomUUID(),
                "a".repeat(64),
                640,
                640,
                List.of(AiModelVersionClass.create(
                        UUID.randomUUID(), versionId, 0, "HEALTHY", "Healthy", AiModelClassKind.HEALTHY)),
                status,
                "{}",
                now,
                UUID.randomUUID(),
                now,
                UUID.randomUUID(),
                null,
                null);
    }

    private AiModel model(UUID modelId, UUID cropTypeId, UUID actorId, Instant now) {
        return AiModel.reconstruct(
                modelId,
                "RICE_MODEL",
                "Rice model",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(cropTypeId),
                AiModelStatus.DRAFT,
                now,
                actorId,
                now,
                actorId,
                null,
                null);
    }
}
