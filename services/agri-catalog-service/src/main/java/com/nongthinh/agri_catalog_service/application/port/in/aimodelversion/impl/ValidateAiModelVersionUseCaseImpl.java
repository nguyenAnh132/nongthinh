package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationClass;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationRequest;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationResult;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.ValidateAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionValidator;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.service.ValidationReportSanitizer;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateAiModelVersionUseCaseImpl implements ValidateAiModelVersionUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final AiModelVersionValidator aiModelVersionValidator;
    private final ValidationReportSanitizer validationReportSanitizer;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelVersionView execute(UUID versionId) {
        AiModelVersion version = support.requireVersion(versionId);
        support.requireModelCanAcceptVersion(support.requireModel(version.getModelId()));
        support.requireActivePrivateArtifact(version.getArtifactFileId());
        version.startValidation();

        AiModelValidationResult result;
        try {
            result = aiModelVersionValidator.validate(new AiModelValidationRequest(
                    version.getId(),
                    version.getArtifactFileId(),
                    version.getArtifactSha256(),
                    version.getInputWidth(),
                    version.getInputHeight(),
                    version.getClasses().stream()
                            .map(item -> new AiModelValidationClass(
                                    item.getClassIndex(), item.getClassCode(), item.getClassKind().name()))
                            .toList()
            ));
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_UNAVAILABLE, ex);
        }

        Instant now = clockProvider.now();
        version.completeValidation(
                result != null && result.valid(),
                validationReportSanitizer.sanitize(result == null ? null : result.report()),
                currentUserProvider.getCurrentUser().getUserId(),
                now
        );
        return AiModelVersionView.from(aiModelVersionRepository.save(version));
    }
}
