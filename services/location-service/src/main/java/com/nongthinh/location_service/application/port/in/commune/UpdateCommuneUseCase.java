package com.nongthinh.location_service.application.port.in.commune;

import java.util.UUID;
import com.nongthinh.location_service.application.command.CommuneUpdateCommand;
import com.nongthinh.location_service.application.view.CommuneView;

public interface UpdateCommuneUseCase {

    CommuneView execute(UUID id, CommuneUpdateCommand command);
}
