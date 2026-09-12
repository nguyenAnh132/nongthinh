package com.nongthinh.notification_service.application.port.in.emailhistory;

import java.util.List;
import com.nongthinh.notification_service.application.view.EmailHistoryView;

public interface GetEmailHistoryByPurposeId {

    List<EmailHistoryView> execute(Long purposeId);

}
