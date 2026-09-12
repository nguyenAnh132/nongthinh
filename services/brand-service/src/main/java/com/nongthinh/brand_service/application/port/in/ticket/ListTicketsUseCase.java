package com.nongthinh.brand_service.application.port.in.ticket;

import java.util.List;
import com.nongthinh.brand_service.application.view.TicketView;

public interface ListTicketsUseCase {

    List<TicketView> execute();
}
