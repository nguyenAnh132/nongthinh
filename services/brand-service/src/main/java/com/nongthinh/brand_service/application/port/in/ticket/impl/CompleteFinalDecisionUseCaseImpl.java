package com.nongthinh.brand_service.application.port.in.ticket.impl;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.command.CompleteFinalDecisionCommand;
import com.nongthinh.brand_service.application.port.in.ticket.CompleteFinalDecisionUseCase;
import com.nongthinh.brand_service.common.currentuser.CurrentUser;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompleteFinalDecisionUseCaseImpl implements CompleteFinalDecisionUseCase {

    private static final String OUTCOME_APPROVED = "APPROVED";
    private static final String OUTCOME_REJECTED = "REJECTED";

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final CamundaTaskSupport camundaTaskSupport;

    @Override
    @Transactional
    public void execute(String taskId, CompleteFinalDecisionCommand command) {
        CurrentUser admin = currentUserProvider.getCurrentUser();
        Task task = camundaTaskSupport.requireTask(taskId);
        camundaTaskSupport.requireTaskDefinitionKey(task, CamundaProcessConstants.FINAL_DECISION_TASK_KEY);
        camundaTaskSupport.requireAssignee(task, admin.getUserId().toString());

        String outcome = command.outcome().trim().toUpperCase();
        if (!OUTCOME_APPROVED.equals(outcome) && !OUTCOME_REJECTED.equals(outcome)) {
            throw new BusinessException(ErrorCode.OUTCOME_INVALID);
        }

        if (OUTCOME_REJECTED.equals(outcome) && isBlank(command.rejectionReason())) {
            throw new BusinessException(ErrorCode.REJECTION_REASON_REQUIRED);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaProcessConstants.OUTCOME_VARIABLE, outcome);
        variables.put(CamundaProcessConstants.REVIEWER_ID_VARIABLE, admin.getUserId().toString());
        variables.put(
                CamundaProcessConstants.REJECTION_REASON_VARIABLE,
                OUTCOME_REJECTED.equals(outcome) ? command.rejectionReason().trim() : ""
        );

        taskService.complete(taskId, variables);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
