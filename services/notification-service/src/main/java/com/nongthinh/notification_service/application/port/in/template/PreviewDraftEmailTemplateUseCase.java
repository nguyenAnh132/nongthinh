package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.command.template.PreviewEmailTemplateCommand;
import com.nongthinh.notification_service.application.view.PreviewEmailTemplateView;

public interface PreviewDraftEmailTemplateUseCase {

    PreviewEmailTemplateView execute(Long purposeId, PreviewEmailTemplateCommand command);
}
