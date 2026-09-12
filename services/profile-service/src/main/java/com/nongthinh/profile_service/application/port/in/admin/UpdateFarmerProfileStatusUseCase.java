package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.presentation.dto.request.admin.ProfileStatusUpdateRequest;

public interface UpdateFarmerProfileStatusUseCase {

    FarmerProfileView execute(UUID id, ProfileStatusUpdateRequest request);
}
