package com.nongthinh.location_service.application.port.in.commune;

import java.util.UUID;
import com.nongthinh.location_service.application.view.CommuneView;

public interface GetCommuneByIdUseCase {

    CommuneView execute(UUID id);
}
