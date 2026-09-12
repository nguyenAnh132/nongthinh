package com.nongthinh.brand_service.application.view;

import com.nongthinh.brand_service.infra.client.profileservice.AdminBrandProfileDetailDto;

public record TicketDetailView(
        TicketView ticket,
        AdminBrandProfileDetailDto profileDetail,
        BrandApprovalProcessView approvalProcess
) {
}
