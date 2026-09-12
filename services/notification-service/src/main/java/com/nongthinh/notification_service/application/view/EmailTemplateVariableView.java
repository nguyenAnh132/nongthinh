package com.nongthinh.notification_service.application.view;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;

public record EmailTemplateVariableView(
        String variableName,
        String description,
        String exampleValue,
        boolean required
) {
    public static EmailTemplateVariableView fromDomain (EmailTemplateVariable emailTemplateVariable) {
        return new EmailTemplateVariableView(
                emailTemplateVariable.getVariableName(),
                emailTemplateVariable.getDescription(),
                emailTemplateVariable.getExampleValue(),
                emailTemplateVariable.isRequired()
        );
    }
}