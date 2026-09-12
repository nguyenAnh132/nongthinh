package com.nongthinh.location_service.application.port.in.commune;

import com.nongthinh.location_service.application.command.CommuneCreationCommand;
import com.nongthinh.location_service.application.view.CommuneView;

public interface CreateCommuneUseCase {

    CommuneView execute(CommuneCreationCommand command);
}
