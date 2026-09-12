package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.port.in.template.PreviewEmailTemplateByIdUseCase;
import com.nongthinh.notification_service.application.port.in.template.support.EmailTemplateSupport;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreviewEmailTemplateByIdUseCaseImpl implements PreviewEmailTemplateByIdUseCase {

    private final EmailTemplateRepository templateRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer templateRenderer;

    @Override
    public PreviewEmailTemplateView execute(Long templateId) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));

        RenderedEmailContent content = templateRenderer.render(
                template.getSubject(),
                template.getHtmlContent(),
                template.getTextContent(),
                EmailTemplateSupport.toExampleVariables(
                        variableRepository.findByPurposeId(template.getPurposeId())));
        return PreviewEmailTemplateView.fromDomain(content);
    }
}
