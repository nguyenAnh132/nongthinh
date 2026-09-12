package com.nongthinh.notification_service.application.port.in.template;

import com.nongthinh.notification_service.application.view.EmailTemplateView;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import java.util.List;

public interface ListEmailTemplatesByPurposeIdUseCase {

    List<EmailTemplateView> execute(Long purposeId);
}
