package com.nongthinh.notification_service.application.port.in.purpose;

import com.nongthinh.notification_service.application.view.EmailTemplatePurposeView;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import java.util.List;

public interface ListEmailTemplatePurposesUseCase {

    List<EmailTemplatePurposeView> execute();
}
