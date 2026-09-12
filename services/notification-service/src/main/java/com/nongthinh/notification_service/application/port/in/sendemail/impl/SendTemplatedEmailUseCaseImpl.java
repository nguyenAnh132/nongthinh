package com.nongthinh.notification_service.application.port.in.sendemail.impl;

import com.nongthinh.notification_service.application.command.SendTemplatedEmailCommand;
import com.nongthinh.notification_service.application.port.in.sendemail.SendTemplatedEmailUseCase;
import com.nongthinh.notification_service.application.port.out.ClockProvider;
import com.nongthinh.notification_service.application.port.out.EmailSender;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.common.constant.EmailHistoryStatusConstant;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendTemplatedEmailUseCaseImpl implements SendTemplatedEmailUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final EmailSender emailSender;
    private final EmailHistoryRepository emailHistoryRepository;
    private final ClockProvider clock;

    @Override
    public void execute(SendTemplatedEmailCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (command.email() == null || command.email().isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_TO_REQUIRED);
        }

        Instant now = clock.now();
        var purpose = purposeRepository.findByCode(command.purposeCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND));

        EmailTemplate template = emailTemplateRepository.findActiveByPurposeId(purpose.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVE_EMAIL_TEMPLATE_NOT_FOUND));

        List<EmailTemplateVariable> allowedVariables = variableRepository.findByPurposeId(purpose.getId());
        Map<String, Object> variables = command.variables() == null ? Map.of() : command.variables();

        emailTemplateRenderer.validateRequiredVariables(variables, allowedVariables);

        RenderedEmailContent rendered = emailTemplateRenderer.render(
                template.getSubject(),
                template.getHtmlContent(),
                template.getTextContent(),
                variables
        );

        String content = rendered.htmlContent() != null && !rendered.htmlContent().isBlank()
                ? rendered.htmlContent()
                : rendered.textContent();

        String sendEmailStatus = EmailHistoryStatusConstant.FAILED;
        try {
            emailSender.sendEmail(command.email(), rendered.subject(), content);
            sendEmailStatus = EmailHistoryStatusConstant.SENT;
        } catch (Exception ex) {
            sendEmailStatus = EmailHistoryStatusConstant.FAILED;
        } finally {
            EmailHistory emailHistory = EmailHistory.create(
                    command.userId(),
                    template.getId(),
                    purpose.getId(),
                    sendEmailStatus,
                    now
            );
            emailHistoryRepository.save(emailHistory);
        }
    }
}
