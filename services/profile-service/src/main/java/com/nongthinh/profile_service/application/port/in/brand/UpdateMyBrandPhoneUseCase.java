package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface UpdateMyBrandPhoneUseCase {

    BrandProfileView execute(String phone);
}
