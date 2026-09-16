package com.nongthinh.bo_portal_service.application.port.in.fileconfig.impl;

import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.nongthinh.bo_portal_service.application.command.UpdateFilePurposeTypeCommand;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.UpdateFilePurposeTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import com.nongthinh.bo_portal_service.application.port.out.repository.FilePurposeTypeRepository;
import com.nongthinh.bo_portal_service.application.view.FilePurposeTypeView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurpose;

@Service
@RequiredArgsConstructor
public class UpdateFilePurposeTypeUseCaseImpl implements UpdateFilePurposeTypeUseCase {
    private final FilePurposeTypeRepository repository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public FilePurposeTypeView execute(String rawPurpose, String typeCode, UpdateFilePurposeTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (command.enabled() == null) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ENABLED_REQUIRED);
        }
        FilePurpose purpose = FilePurpose.from(rawPurpose);
        String code = typeCode == null ? "" : typeCode.trim().toUpperCase(Locale.ROOT);
        var mapping = repository.findByPurposeAndTypeCode(purpose, code)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_PURPOSE_TYPE_NOT_FOUND));
        mapping.changeEnabled(command.enabled(), clockProvider.now());
        return FilePurposeTypeView.from(repository.save(mapping));
    }
}
