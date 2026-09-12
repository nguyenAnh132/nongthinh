package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.application.command.farmer.UpdateMyFarmerAddressCommand;
import com.nongthinh.profile_service.application.view.FarmerProfileView;

public interface UpdateMyFarmerAddressUseCase {

    FarmerProfileView execute(UpdateMyFarmerAddressCommand command);
}
