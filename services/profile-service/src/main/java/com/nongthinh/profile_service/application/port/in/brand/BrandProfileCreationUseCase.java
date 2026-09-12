package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;

public interface BrandProfileCreationUseCase {

    void execute(BrandProfileCreationCommand command);
}