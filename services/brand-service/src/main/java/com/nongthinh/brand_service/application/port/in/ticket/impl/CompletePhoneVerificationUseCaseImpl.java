package com.nongthinh.brand_service.application.port.in.ticket.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.command.CompletePhoneVerificationCommand;
import com.nongthinh.brand_service.application.port.in.ticket.CompletePhoneVerificationUseCase;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.common.currentuser.CurrentUser;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import com.nongthinh.brand_service.infra.client.profileservice.CreateVerificationLogParam;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompletePhoneVerificationUseCaseImpl implements CompletePhoneVerificationUseCase {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final CamundaTaskSupport camundaTaskSupport;
    private final ProfileServicePort profileServicePort;

    @Override
    @Transactional
    public void execute(String taskId, CompletePhoneVerificationCommand command) {
        CurrentUser admin = currentUserProvider.getCurrentUser();
        Task task = camundaTaskSupport.requireTask(taskId);
        camundaTaskSupport.requireTaskDefinitionKey(task, CamundaProcessConstants.PHONE_VERIFICATION_TASK_KEY);
        camundaTaskSupport.requireAssignee(task, admin.getUserId().toString());

        UUID brandProfileId = camundaTaskSupport.requireBrandProfileId(task);

        profileServicePort.createVerificationLog(
                brandProfileId,
                new CreateVerificationLogParam(
                        admin.getUserId(),
                        command.phoneCalled(),
                        command.result(),
                        command.note()
                )
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaProcessConstants.VERIFICATION_RESULT_VARIABLE, command.result());
        variables.put(CamundaProcessConstants.REVIEWER_ID_VARIABLE, admin.getUserId().toString());
        variables.put(
                CamundaProcessConstants.VERIFICATION_NOTE_VARIABLE,
                command.note() == null ? "" : command.note()
        );

        taskService.complete(taskId, variables);
    }
}
