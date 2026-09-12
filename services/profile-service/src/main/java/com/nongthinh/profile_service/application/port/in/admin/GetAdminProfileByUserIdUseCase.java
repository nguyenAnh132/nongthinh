package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.AdminProfileView;

public interface GetAdminProfileByUserIdUseCase {

    AdminProfileView execute(UUID userId);
}
