package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.AdminBrandProfileDetailView;

public interface GetAdminBrandProfileDetailUseCase {

    AdminBrandProfileDetailView execute(UUID id);
}
