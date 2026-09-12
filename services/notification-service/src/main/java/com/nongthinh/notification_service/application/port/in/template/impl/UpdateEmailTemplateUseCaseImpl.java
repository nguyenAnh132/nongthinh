package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.command.template.UpdateEmailTemplateCommand;
import com.nongthinh.notification_service.application.port.in.template.UpdateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.support.EmailTemplateSupport;
import com.nongthinh.notification_service.application.port.out.ClockProvider;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEmailTemplateUseCaseImpl implements UpdateEmailTemplateUseCase {

    private final EmailTemplateRepository templateRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer templateRenderer;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public EmailTemplateView execute(Long templateId, UpdateEmailTemplateCommand command) {
        Objects.requireNonNull(command, "command is required");

        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));

        EmailTemplateSupport.validateContent(command.subject(), command.htmlContent(), command.textContent());
        templateRenderer.validatePlaceholders(
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                variableRepository.findByPurposeId(template.getPurposeId()));

        template.updateContent(
                command.name(),
                command.description(),
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                clockProvider.now());

        templateRepository.save(template);
        return EmailTemplateView.fromDomain(template);
    }
}
