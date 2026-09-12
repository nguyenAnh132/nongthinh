package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.event.BrandProfileCreationRequestedEvent;

public interface HandleBrandProfileCreationRequestedUseCase {

    void execute(BrandProfileCreationRequestedEvent event);
}
