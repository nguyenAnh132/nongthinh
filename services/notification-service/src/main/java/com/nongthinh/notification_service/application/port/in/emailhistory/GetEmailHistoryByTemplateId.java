package com.nongthinh.notification_service.application.port.in.emailhistory;

import java.util.List;
import com.nongthinh.notification_service.application.view.EmailHistoryView;

public interface GetEmailHistoryByTemplateId {

    List<EmailHistoryView> execute(Long templateId);

}
