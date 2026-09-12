package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.command.brand.UpdateMyBrandOfficeAddressCommand;
import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface UpdateMyBrandOfficeAddressUseCase {

    BrandProfileView execute(UpdateMyBrandOfficeAddressCommand command);
}
