package com.nongthinh.brand_service.application.port.in.ticket.impl;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.port.in.ticket.ClaimTicketUseCase;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimTicketUseCaseImpl implements ClaimTicketUseCase {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final CamundaTaskSupport camundaTaskSupport;
    private final ProfileServicePort profileServicePort;

    @Override
    @Transactional
    public void execute(String taskId) {
        String userId = currentUserProvider.getCurrentUser().getUserId().toString();
        Task task = camundaTaskSupport.requireTask(taskId);

        if (task.getAssignee() != null) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_CLAIMED);
        }

        taskService.claim(taskId, userId);

        if (CamundaProcessConstants.PHONE_VERIFICATION_TASK_KEY.equals(task.getTaskDefinitionKey())) {
            UUID brandProfileId = camundaTaskSupport.requireBrandProfileId(task);
            profileServicePort.updateBrandProfileStatus(brandProfileId, "UNDER_REVIEW", null, null);
        }
    }
}
