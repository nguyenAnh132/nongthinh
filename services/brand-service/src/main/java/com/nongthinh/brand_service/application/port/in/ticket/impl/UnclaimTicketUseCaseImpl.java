package com.nongthinh.brand_service.application.port.in.ticket.impl;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.port.in.ticket.UnclaimTicketUseCase;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnclaimTicketUseCaseImpl implements UnclaimTicketUseCase {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final CamundaTaskSupport camundaTaskSupport;

    @Override
    @Transactional
    public void execute(String taskId) {
        String userId = currentUserProvider.getCurrentUser().getUserId().toString();
        Task task = camundaTaskSupport.requireTask(taskId);
        camundaTaskSupport.requireAssignee(task, userId);
        taskService.setAssignee(taskId, null);
    }
}
