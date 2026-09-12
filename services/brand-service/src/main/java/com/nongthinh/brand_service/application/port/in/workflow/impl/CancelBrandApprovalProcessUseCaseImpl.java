package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.port.in.workflow.CancelBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.ClockProvider;
import com.nongthinh.brand_service.application.port.out.repository.BrandApprovalProcessRepository;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelBrandApprovalProcessUseCaseImpl implements CancelBrandApprovalProcessUseCase {

    private final BrandApprovalProcessRepository brandApprovalProcessRepository;
    private final RuntimeService runtimeService;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID brandProfileId, UUID actorUserId, String reason) {
        brandApprovalProcessRepository.findByBrandProfileId(brandProfileId).ifPresent(process -> {
            if (process.getStatus() != BrandApprovalProcessStatus.STARTED) {
                return;
            }

            try {
                runtimeService.deleteProcessInstance(
                        process.getCamundaProcessInstanceId(),
                        reason == null ? "Brand rejected early" : reason
                );
            } catch (Exception ex) {
                log.warn(
                        "Failed to delete Camunda process instance {} for brandProfileId={}",
                        process.getCamundaProcessInstanceId(),
                        brandProfileId,
                        ex
                );
            }

            BrandApprovalProcess cancelled = process.cancel(actorUserId, clockProvider.now());
            brandApprovalProcessRepository.save(cancelled);
            log.info("Cancelled brand approval process for brandProfileId={}", brandProfileId);
        });
    }
}
