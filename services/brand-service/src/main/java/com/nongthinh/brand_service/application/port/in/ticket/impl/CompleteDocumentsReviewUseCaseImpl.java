package com.nongthinh.brand_service.application.port.in.ticket.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.command.CompleteDocumentsReviewCommand;
import com.nongthinh.brand_service.application.port.in.ticket.CompleteDocumentsReviewUseCase;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.common.currentuser.CurrentUser;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompleteDocumentsReviewUseCaseImpl implements CompleteDocumentsReviewUseCase {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final CamundaTaskSupport camundaTaskSupport;
    private final ProfileServicePort profileServicePort;

    @Override
    @Transactional
    public void execute(String taskId, CompleteDocumentsReviewCommand command) {
        CurrentUser admin = currentUserProvider.getCurrentUser();
        Task task = camundaTaskSupport.requireTask(taskId);
        camundaTaskSupport.requireTaskDefinitionKey(task, CamundaProcessConstants.DOCUMENTS_REVIEW_TASK_KEY);
        camundaTaskSupport.requireAssignee(task, admin.getUserId().toString());

        if (!command.documentsOk() && isBlank(command.revisionReason())) {
            throw new BusinessException(ErrorCode.REVISION_REASON_REQUIRED);
        }

        UUID brandProfileId = camundaTaskSupport.requireBrandProfileId(task);

        profileServicePort.reviewBrandDocuments(
                brandProfileId,
                admin.getUserId(),
                command.documentsOk(),
                command.revisionReason()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaProcessConstants.DOCUMENTS_OK_VARIABLE, command.documentsOk());
        variables.put(CamundaProcessConstants.REVIEWER_ID_VARIABLE, admin.getUserId().toString());
        variables.put(
                CamundaProcessConstants.REVISION_REASON_VARIABLE,
                command.documentsOk() || command.revisionReason() == null ? "" : command.revisionReason().trim()
        );

        taskService.complete(taskId, variables);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
