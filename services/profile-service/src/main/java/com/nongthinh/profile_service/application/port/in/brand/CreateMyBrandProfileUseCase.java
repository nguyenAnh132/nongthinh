package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileCreationRequest;

public interface CreateMyBrandProfileUseCase {

    void execute(MyBrandProfileCreationRequest request);
}
