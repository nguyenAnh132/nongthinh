package com.nongthinh.notification_service.application.port.in.sendemail.impl;

import com.nongthinh.notification_service.application.command.SendRegisterOtpCommand;
import com.nongthinh.notification_service.application.port.in.sendemail.SendRegisterOtpUseCase;
import com.nongthinh.notification_service.application.port.out.ClockProvider;
import com.nongthinh.notification_service.application.port.out.EmailSender;
import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.common.constant.EmailHistoryStatusConstant;
import com.nongthinh.notification_service.common.constant.EmailPurposeConstant;
import com.nongthinh.notification_service.common.constant.RegisterVariableConstant;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendRegisterOtpUseCaseImpl implements SendRegisterOtpUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateVariableRepository variableRepository;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final EmailSender emailSender;
    private final EmailHistoryRepository emailHistoryRepository;
    private final ClockProvider clock;

    @Override
    public void execute(SendRegisterOtpCommand command) {

        Instant now = clock.now();

        Objects.requireNonNull(command, "command is required");

        if (command.email() == null || command.email().isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_TO_REQUIRED);
        }

        if (command.otp() == null || command.otp().isBlank()) {
            throw new BusinessException(ErrorCode.OTP_REQUIRED);
        }

        var purpose = purposeRepository.findByCode(EmailPurposeConstant.REGISTER_OTP)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND));

        EmailTemplate template = emailTemplateRepository.findActiveByPurposeId(purpose.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVE_EMAIL_TEMPLATE_NOT_FOUND));

        List<EmailTemplateVariable> allowedVariables = variableRepository.findByPurposeId(purpose.getId());

        Map<String, Object> variables = mapVariables(command);

        emailTemplateRenderer.validateRequiredVariables(variables,allowedVariables);

        RenderedEmailContent rendered = emailTemplateRenderer.render(
                template.getSubject(),
                template.getHtmlContent(),
                template.getTextContent(),
                variables);

        String content = rendered.htmlContent() != null && !rendered.htmlContent().isBlank()
                ? rendered.htmlContent()
                : rendered.textContent();

        String sendEmailSuccess = EmailHistoryStatusConstant.FAILED;

        try {

            emailSender.sendEmail(command.email(), rendered.subject(), content);
            sendEmailSuccess = EmailHistoryStatusConstant.SENT;
        } catch (Exception ex) {
            sendEmailSuccess = EmailHistoryStatusConstant.FAILED;
        } finally {
            EmailHistory emailHistory = EmailHistory.create(
                command.userId(),
                template.getId(),
                purpose.getId(),
                sendEmailSuccess,
                now
            );
            emailHistoryRepository.save(emailHistory);
        }
        
    }

    private Map<String, Object> mapVariables(SendRegisterOtpCommand command) {
        Map<String, Object> result = new HashMap<>();
        result.put(RegisterVariableConstant.USER_NAME, command.userName());
        result.put(RegisterVariableConstant.OTP_CODE, command.otp());
        result.put(RegisterVariableConstant.EXPIRE_MINUTES, command.expireMinutes());

        return result;
    }
}
