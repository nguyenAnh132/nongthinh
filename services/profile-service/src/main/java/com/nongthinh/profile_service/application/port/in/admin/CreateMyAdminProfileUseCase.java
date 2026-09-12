package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileCreationRequest;

public interface CreateMyAdminProfileUseCase {

    void execute(MyAdminProfileCreationRequest request);
}
