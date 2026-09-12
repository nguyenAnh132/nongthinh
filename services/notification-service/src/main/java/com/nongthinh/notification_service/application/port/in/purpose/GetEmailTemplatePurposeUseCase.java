package com.nongthinh.notification_service.application.port.in.purpose;

import com.nongthinh.notification_service.application.view.EmailTemplatePurposeView;
public interface GetEmailTemplatePurposeUseCase {

    EmailTemplatePurposeView execute(Long id);
}
