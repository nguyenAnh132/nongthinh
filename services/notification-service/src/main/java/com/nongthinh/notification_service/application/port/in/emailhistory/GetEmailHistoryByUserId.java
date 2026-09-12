package com.nongthinh.notification_service.application.port.in.emailhistory;

import java.util.List;
import com.nongthinh.notification_service.application.view.EmailHistoryView;
import java.util.UUID;

public interface GetEmailHistoryByUserId {

    List<EmailHistoryView> execute(UUID userId);

}
