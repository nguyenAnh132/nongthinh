package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.command.template.CreateEmailTemplateCommand;
import com.nongthinh.notification_service.application.view.EmailTemplateView;

public interface CreateEmailTemplateUseCase {

    EmailTemplateView execute(Long purposeId, CreateEmailTemplateCommand command);
}
