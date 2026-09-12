package com.nongthinh.profile_service.application.port.in.brand;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.BrandProfilePublicView;

public interface GetPublicBrandProfileByUserIdUseCase {

    BrandProfilePublicView execute(UUID userId);
}
