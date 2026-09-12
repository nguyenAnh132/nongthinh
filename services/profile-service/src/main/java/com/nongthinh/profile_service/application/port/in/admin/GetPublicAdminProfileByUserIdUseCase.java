package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.AdminProfilePublicView;

public interface GetPublicAdminProfileByUserIdUseCase {

    AdminProfilePublicView execute(UUID userId);
}
