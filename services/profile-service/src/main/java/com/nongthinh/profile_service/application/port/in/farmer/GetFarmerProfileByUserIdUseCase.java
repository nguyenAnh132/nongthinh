package com.nongthinh.profile_service.application.port.in.farmer;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.FarmerProfileView;

public interface GetFarmerProfileByUserIdUseCase {

    FarmerProfileView execute(UUID userId);
}
