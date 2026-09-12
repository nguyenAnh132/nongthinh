package com.nongthinh.notification_service.application.port.in.template.support;

import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EmailTemplateSupport {   

    private EmailTemplateSupport() {
    }

    public static void validateContent(String subject, String htmlContent, String textContent) {
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(ErrorCode.TEMPLATE_SUBJECT_REQUIRED);
        }

        if ((htmlContent == null || htmlContent.isBlank()) && (textContent == null || textContent.isBlank())) {
            throw new BusinessException(ErrorCode.TEMPLATE_CONTENT_REQUIRED);
        }
    }

    public static Map<String, Object> toExampleVariables(List<EmailTemplateVariable> variables) {
        Map<String, Object> result = new HashMap<>();

        for (EmailTemplateVariable variable : variables) {
            result.put(variable.getVariableName(), variable.getExampleValue());
        }

        return result;
    }
}
