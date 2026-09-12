package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileUpdateRequest;

public interface UpdateMyBrandProfileUseCase {

    BrandProfileView execute(MyBrandProfileUpdateRequest request);
}
