package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.command.template.PreviewEmailTemplateCommand;
import com.nongthinh.notification_service.application.port.in.template.PreviewDraftEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.in.template.support.EmailTemplateSupport;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreviewDraftEmailTemplateUseCaseImpl implements PreviewDraftEmailTemplateUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer templateRenderer;

    @Override
    public PreviewEmailTemplateView execute(Long purposeId, PreviewEmailTemplateCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (purposeRepository.findById(purposeId).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND);
        }

        List<EmailTemplateVariable> variables = variableRepository.findByPurposeId(purposeId);
        templateRenderer.validatePlaceholders(
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                variables);

        RenderedEmailContent content = templateRenderer.render(
                command.subject(),
                command.htmlContent(),
                command.textContent(),
                EmailTemplateSupport.toExampleVariables(variables));

        return PreviewEmailTemplateView.fromDomain(content);
    }
}
