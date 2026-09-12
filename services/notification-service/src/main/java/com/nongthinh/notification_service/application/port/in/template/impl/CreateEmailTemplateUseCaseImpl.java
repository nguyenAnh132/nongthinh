package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.command.template.CreateEmailTemplateCommand;
import com.nongthinh.notification_service.application.port.in.template.CreateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.support.EmailTemplateSupport;
import com.nongthinh.notification_service.application.port.out.ClockProvider;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateEmailTemplateUseCaseImpl implements CreateEmailTemplateUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateRepository templateRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer templateRenderer;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public EmailTemplateView execute(Long purposeId, CreateEmailTemplateCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (purposeRepository.findById(purposeId).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND);
        }

        EmailTemplateSupport.validateContent(command.subject(), command.htmlContent(), command.textContent());
        templateRenderer.validatePlaceholders(
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                variableRepository.findByPurposeId(purposeId));

        Instant now = clockProvider.now();
        EmailTemplate template = EmailTemplate.create(
                purposeId,
                command.name(),
                command.description(),
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                now);

        templateRepository.save(template);
        return EmailTemplateView.fromDomain(template);
    }
}
