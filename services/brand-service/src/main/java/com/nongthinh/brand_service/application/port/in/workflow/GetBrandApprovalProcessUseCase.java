package com.nongthinh.brand_service.application.port.in.workflow;

import java.util.UUID;
import com.nongthinh.brand_service.application.view.BrandApprovalProcessView;

public interface GetBrandApprovalProcessUseCase {

    BrandApprovalProcessView execute(UUID brandProfileId);
}
