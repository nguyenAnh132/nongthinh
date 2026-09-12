package com.nongthinh.profile_service.application.port.in.farmer;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.FarmerProfilePublicView;

public interface GetPublicFarmerProfileByIdUseCase {

    FarmerProfilePublicView execute(UUID id);
}
