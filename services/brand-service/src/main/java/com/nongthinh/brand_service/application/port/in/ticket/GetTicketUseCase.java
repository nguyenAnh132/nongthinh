package com.nongthinh.brand_service.application.port.in.ticket;

import com.nongthinh.brand_service.application.view.TicketDetailView;

public interface GetTicketUseCase {

    TicketDetailView execute(String taskId);
}
