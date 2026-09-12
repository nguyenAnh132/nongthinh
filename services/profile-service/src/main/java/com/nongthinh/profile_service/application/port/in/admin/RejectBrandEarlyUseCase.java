package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface RejectBrandEarlyUseCase {

    BrandProfileView execute(UUID brandProfileId, UUID actorUserId, String rejectionReason);
}
