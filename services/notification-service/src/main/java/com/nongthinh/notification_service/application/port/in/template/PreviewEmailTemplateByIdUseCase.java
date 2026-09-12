package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;

public interface PreviewEmailTemplateByIdUseCase {

    PreviewEmailTemplateView execute(Long templateId);
}
