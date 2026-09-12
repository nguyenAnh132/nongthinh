package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.command.template.UpdateEmailTemplateCommand;
import com.nongthinh.notification_service.application.view.EmailTemplateView;

public interface UpdateEmailTemplateUseCase {

    EmailTemplateView execute(Long templateId, UpdateEmailTemplateCommand command);
}
