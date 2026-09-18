package com.nongthinh.bo_portal_service.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.stream.Collectors;
import com.nongthinh.bo_portal_service.application.command.UpdateFilePurposeTypeCommand;
import com.nongthinh.bo_portal_service.application.view.FilePurposeTypeView;
import com.nongthinh.bo_portal_service.application.view.FileUploadPolicyView;
import com.nongthinh.bo_portal_service.application.view.UploadPolicyView;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateFilePurposeTypeRequest;

@Mapper(componentModel = "spring")
public interface FileConfigurationMapper {
    UpdateFilePurposeTypeCommand toUpdateCommand(UpdateFilePurposeTypeRequest request);

    @Mapping(target = "allowedExtensions", expression = "java(toAllowedExtensions(policy))")
    UploadPolicyView toUploadPolicyView(FileUploadPolicyView policy);

    default Set<String> toAllowedExtensions(FileUploadPolicyView policy) {
        return policy.allowedContentTypes() == null ? null : policy.fileTypes().stream()
                .filter(FilePurposeTypeView::enabled)
                .map(FilePurposeTypeView::extension)
                .collect(Collectors.toUnmodifiableSet());
    }
}
