package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import com.nongthinh.brand_service.application.port.in.workflow.GetBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.repository.BrandApprovalProcessRepository;
import com.nongthinh.brand_service.application.view.BrandApprovalProcessView;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBrandApprovalProcessUseCaseImpl implements GetBrandApprovalProcessUseCase {

    private final BrandApprovalProcessRepository brandApprovalProcessRepository;
    private final TaskService taskService;
    private final RuntimeService runtimeService;

    @Override
    public BrandApprovalProcessView execute(UUID brandProfileId) {
        BrandApprovalProcess process = brandApprovalProcessRepository.findByBrandProfileId(brandProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_PROCESS_NOT_FOUND));

        List<String> activeTaskDefinitionKeys = List.of();
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(process.getCamundaProcessInstanceId())
                .singleResult() != null) {
            activeTaskDefinitionKeys = taskService.createTaskQuery()
                    .processInstanceId(process.getCamundaProcessInstanceId())
                    .list()
                    .stream()
                    .map(Task::getTaskDefinitionKey)
                    .toList();
        }

        return new BrandApprovalProcessView(
                process.getId(),
                process.getBrandProfileId(),
                process.getCamundaProcessInstanceId(),
                process.getCamundaBusinessKey(),
                process.getStatus().getValue(),
                process.getAssignedReviewerId(),
                process.getStartedAt(),
                process.getCompletedAt(),
                activeTaskDefinitionKeys
        );
    }
}
