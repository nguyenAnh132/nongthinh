package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.util.Map;
import java.util.Objects;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.command.StartBrandApprovalProcessCommand;
import com.nongthinh.brand_service.application.port.in.workflow.StartBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.ClockProvider;
import com.nongthinh.brand_service.application.port.out.IdGenerator;
import com.nongthinh.brand_service.application.port.out.repository.BrandApprovalProcessRepository;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartBrandApprovalProcessUseCaseImpl implements StartBrandApprovalProcessUseCase {

    private static final String PROCESS_DEFINITION_KEY = "brand-approval";

    private final RuntimeService runtimeService;
    private final BrandApprovalProcessRepository brandApprovalProcessRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(StartBrandApprovalProcessCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (brandApprovalProcessRepository.existsByBrandProfileId(command.brandProfileId())) {
            return;
        }

        String businessKey = "brand-" + command.brandProfileId();
        Map<String, Object> variables = Map.of(
                "brandProfileId", command.brandProfileId().toString(),
                "userId", command.userId().toString(),
                "brandName", command.brandName()
        );

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                PROCESS_DEFINITION_KEY,
                businessKey,
                variables
        );

        BrandApprovalProcess brandApprovalProcess = BrandApprovalProcess.start(
                idGenerator.generate(),
                command.brandProfileId(),
                instance.getProcessInstanceId(),
                businessKey,
                clockProvider.now()
        );

        brandApprovalProcessRepository.save(brandApprovalProcess);

        log.info(
                "Started brand approval process: brandProfileId={}, processInstanceId={}, businessKey={}",
                command.brandProfileId(),
                instance.getProcessInstanceId(),
                businessKey
        );
    }
}
