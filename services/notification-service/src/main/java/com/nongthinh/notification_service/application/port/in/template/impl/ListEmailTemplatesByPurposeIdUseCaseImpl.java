package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.port.in.template.ListEmailTemplatesByPurposeIdUseCase;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListEmailTemplatesByPurposeIdUseCaseImpl implements ListEmailTemplatesByPurposeIdUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateRepository templateRepository;

    @Override
    public List<EmailTemplateView> execute(Long purposeId) {
        if (purposeRepository.findById(purposeId).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND);
        }

        List<EmailTemplate> emailTemplates = templateRepository.findByPurposeId(purposeId);

        return emailTemplates.stream()
                .map(EmailTemplateView::fromDomain)
                .toList();
    }
}
