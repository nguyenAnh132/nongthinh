package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface UpdateMyBrandRepresentativeEmailUseCase {

    BrandProfileView execute(String representativeEmail);
}
