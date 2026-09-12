package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.application.event.FarmerProfileCreationRequestedEvent;

public interface HandleFarmerProfileCreationRequestedUseCase {

    void execute(FarmerProfileCreationRequestedEvent event);
}
