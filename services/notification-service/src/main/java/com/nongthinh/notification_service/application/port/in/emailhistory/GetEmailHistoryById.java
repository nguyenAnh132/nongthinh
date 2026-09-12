package com.nongthinh.notification_service.application.port.in.emailhistory;

import com.nongthinh.notification_service.application.view.EmailHistoryView;

public interface GetEmailHistoryById {

    EmailHistoryView execute(Long id);

}
