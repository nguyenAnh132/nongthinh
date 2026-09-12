package com.nongthinh.brand_service.application.port.in.workflow.support;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.out.ClockProvider;
import com.nongthinh.brand_service.application.port.out.repository.BrandApprovalProcessRepository;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrandApprovalProcessSupport {

    private final BrandApprovalProcessRepository brandApprovalProcessRepository;
    private final ClockProvider clockProvider;

    public void completeIfPresent(UUID brandProfileId, UUID reviewerId) {
        brandApprovalProcessRepository.findByBrandProfileId(brandProfileId)
                .ifPresent(process -> {
                    BrandApprovalProcess completed = process.complete(reviewerId, clockProvider.now());
                    brandApprovalProcessRepository.save(completed);
                });
    }
}
