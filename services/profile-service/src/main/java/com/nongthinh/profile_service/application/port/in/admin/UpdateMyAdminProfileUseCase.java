package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileUpdateRequest;

public interface UpdateMyAdminProfileUseCase {

    AdminProfileView execute(MyAdminProfileUpdateRequest request);
}
