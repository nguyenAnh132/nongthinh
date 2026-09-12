package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.view.AdminProfileView;

public interface UpdateMyAdminAvatarUseCase {

    AdminProfileView execute(String avatarUrl);
}
