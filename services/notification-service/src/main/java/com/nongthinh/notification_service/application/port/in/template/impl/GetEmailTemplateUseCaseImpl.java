package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.port.in.template.GetEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEmailTemplateUseCaseImpl implements GetEmailTemplateUseCase {

    private final EmailTemplateRepository templateRepository;

    @Override
    public EmailTemplateView execute(Long templateId) {
        EmailTemplate emailTemplate = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));

        return EmailTemplateView.fromDomain(emailTemplate);
    }
}
