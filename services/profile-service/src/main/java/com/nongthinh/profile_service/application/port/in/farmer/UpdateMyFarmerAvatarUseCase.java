package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.application.view.FarmerProfileView;

public interface UpdateMyFarmerAvatarUseCase {

    FarmerProfileView execute(String avatarUrl);
}
