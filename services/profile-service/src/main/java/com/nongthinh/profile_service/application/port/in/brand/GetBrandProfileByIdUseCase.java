package com.nongthinh.profile_service.application.port.in.brand;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface GetBrandProfileByIdUseCase {

    BrandProfileView execute(UUID id);
}
