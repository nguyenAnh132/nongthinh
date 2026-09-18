package com.nongthinh.bo_portal_service.application.port.in.fileconfig.impl;

import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.GetFileUploadPolicyUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.FilePurposeTypeRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.view.FileUploadPolicyView;
import com.nongthinh.bo_portal_service.application.view.FilePurposeTypeView;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurpose;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurposeType;

@Service
@RequiredArgsConstructor
public class GetFileUploadPolicyUseCaseImpl implements GetFileUploadPolicyUseCase {
    private final FilePurposeTypeRepository repository;
    private final SystemParamRepository systemParamRepository;

    @Override
    @Transactional(readOnly = true)
    public FileUploadPolicyView execute(String rawPurpose) {
        FilePurpose purpose = FilePurpose.from(rawPurpose);
        Long maxSize = systemParamRepository.findByName(purpose.maxSizeParamName())
                .map(param -> Long.valueOf(param.getValue())).orElse(null);
        var mappings = repository.findByPurpose(purpose);
        // Null means configuration missing; an empty set means explicitly deny all types.
        var allowed = mappings.isEmpty() ? null : mappings.stream()
                .filter(FilePurposeType::isEnabled)
                .map(mapping -> mapping.getFileType().contentType())
                .collect(Collectors.toUnmodifiableSet());
        return new FileUploadPolicyView(purpose.name(), maxSize, allowed,
                mappings.stream().map(FilePurposeTypeView::from).toList());
    }
}
