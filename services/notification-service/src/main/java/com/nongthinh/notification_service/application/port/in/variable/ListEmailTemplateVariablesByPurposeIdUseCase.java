package com.nongthinh.notification_service.application.port.in.variable;

import com.nongthinh.notification_service.application.view.EmailTemplateVariableView;
import java.util.List;

public interface ListEmailTemplateVariablesByPurposeIdUseCase {

    List<EmailTemplateVariableView> execute(Long purposeId);
    
}
