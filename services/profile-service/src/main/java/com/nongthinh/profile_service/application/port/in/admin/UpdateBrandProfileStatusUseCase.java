package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.presentation.dto.request.admin.BrandProfileStatusUpdateRequest;

public interface UpdateBrandProfileStatusUseCase {

    BrandProfileView execute(UUID id, BrandProfileStatusUpdateRequest request);
}
