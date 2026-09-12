package com.nongthinh.notification_service.application.port.out.repository;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import java.util.List;

public interface EmailTemplateVariableRepository {

    List<EmailTemplateVariable> findByPurposeId(Long purposeId);
}
