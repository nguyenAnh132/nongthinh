package com.nongthinh.notification_service.application.port.in.variable.impl;

import com.nongthinh.notification_service.application.port.in.variable.ListEmailTemplateVariablesByPurposeIdUseCase;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.application.view.EmailTemplateVariableView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListEmailTemplateVariablesByPurposeIdUseCaseImpl implements ListEmailTemplateVariablesByPurposeIdUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateVariableRepository variableRepository;

    @Override
    public List<EmailTemplateVariableView> execute(Long purposeId) {
        if (purposeRepository.findById(purposeId).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND);
        }

        return variableRepository.findByPurposeId(purposeId).stream()
                .map(EmailTemplateVariableView::fromDomain)
                .toList();
    }
}
