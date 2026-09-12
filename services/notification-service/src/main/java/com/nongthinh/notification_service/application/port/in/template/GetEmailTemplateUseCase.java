package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.view.EmailTemplateView;

public interface GetEmailTemplateUseCase {

    EmailTemplateView execute(Long templateId);
}
