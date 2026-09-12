package com.nongthinh.notification_service.application.view;

import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;

public record PreviewEmailTemplateView(
        String subject,
        String htmlContent,
        String textContent
) {
    public static PreviewEmailTemplateView fromDomain (RenderedEmailContent content) {
        return new PreviewEmailTemplateView(
                content.subject(),
                content.htmlContent(),
                content.textContent()
        );
    }
}
