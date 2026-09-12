package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelClassManifest;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ResolvedDiseaseMapping;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.ModelRegistryPort;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogModelRegistryAdapter implements ModelRegistryPort {

    private final CatalogClient catalogClient;

    @Value("${spring.security.api-key.clients.agri-catalog-service.api-key}")
    private String catalogApiKey;

    @Override
    public ActiveModelDeployment resolveActiveDeployment(UUID cropTypeId) {
        try {
            CatalogResponse<CatalogDeploymentDto> response = catalogClient.resolveDeployment(cropTypeId, catalogApiKey);
            if (response == null || response.result() == null) {
                throw new DiagnosisException(ErrorCode.MODEL_REGISTRY_UNAVAILABLE);
            }
            CatalogDeploymentDto deployment = response.result();
            return new ActiveModelDeployment(
                    deployment.deploymentId(), deployment.modelId(), deployment.modelCode(), deployment.modelName(),
                    deployment.modelVersionId(), deployment.modelVersion(), deployment.artifactFileId(),
                    deployment.artifactSha256(), deployment.inputWidth(), deployment.inputHeight(),
                    deployment.classes().stream().map(item -> new ModelClassManifest(
                            item.id(), item.classIndex(), item.classCode(), item.displayName(), item.classKind())).toList());
        } catch (FeignException ex) {
            throw translate(ex);
        } catch (DiagnosisException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new DiagnosisException(ErrorCode.MODEL_REGISTRY_UNAVAILABLE, ex);
        }
    }

    @Override
    public List<ResolvedDiseaseMapping> resolveDiseaseMappings(
            UUID modelVersionId, UUID cropTypeId, List<String> classCodes) {
        try {
            CatalogResponse<List<CatalogDiseaseMappingDto>> response = catalogClient.resolveMappings(
                    new CatalogMappingResolutionRequest(modelVersionId, cropTypeId, classCodes), catalogApiKey);
            if (response == null || response.result() == null) {
                throw new DiagnosisException(ErrorCode.MODEL_REGISTRY_UNAVAILABLE);
            }
            return response.result().stream().map(item -> new ResolvedDiseaseMapping(
                    item.classCode(), item.diseaseId(), item.displayName(), item.catalogUpdatedAt())).toList();
        } catch (FeignException ex) {
            throw translate(ex);
        } catch (DiagnosisException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new DiagnosisException(ErrorCode.MODEL_REGISTRY_UNAVAILABLE, ex);
        }
    }

    private DiagnosisException translate(FeignException exception) {
        String body = exception.contentUTF8();
        if (body.contains("MODEL_NOT_AVAILABLE_FOR_CROP")) {
            return new DiagnosisException(ErrorCode.MODEL_NOT_AVAILABLE_FOR_CROP, exception);
        }
        if (body.contains("SYS_CATALOG_MAPPING_NOT_READY")) {
            return new DiagnosisException(ErrorCode.CATALOG_MAPPING_NOT_READY, exception);
        }
        return new DiagnosisException(ErrorCode.MODEL_REGISTRY_UNAVAILABLE, exception);
    }
}
