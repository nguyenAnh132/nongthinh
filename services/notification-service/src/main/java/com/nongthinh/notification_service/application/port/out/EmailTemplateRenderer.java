package com.nongthinh.notification_service.application.port.out;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EmailTemplateRenderer {

    Set<String> extractPlaceholders(String... contents);

    void validatePlaceholders(
            String subject,
            String htmlContent,
            String textContent,
            List<EmailTemplateVariable> allowedVariables);

    void validateRequiredVariables(Map<String, ?> variables, List<EmailTemplateVariable> allowedVariables);

    RenderedEmailContent render(
            String subject,
            String htmlContent,
            String textContent,
            Map<String, ?> variables);
}
