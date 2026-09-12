package com.nongthinh.brand_service.infra.camunda;

import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CamundaTaskSupport {

    private final TaskService taskService;
    private final RuntimeService runtimeService;

    public Task requireTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    public void requireAssignee(Task task, String userId) {
        if (task.getAssignee() == null || !task.getAssignee().equals(userId)) {
            throw new BusinessException(ErrorCode.TASK_NOT_ASSIGNED);
        }
    }

    public UUID requireBrandProfileId(Task task) {
        Object value = runtimeService.getVariable(task.getProcessInstanceId(), CamundaProcessConstants.BRAND_PROFILE_ID_VARIABLE);
        if (value == null) {
            throw new BusinessException(ErrorCode.PROCESS_VARIABLE_MISSING);
        }
        return UUID.fromString(value.toString());
    }

    public void requireTaskDefinitionKey(Task task, String expectedTaskDefinitionKey) {
        if (!expectedTaskDefinitionKey.equals(task.getTaskDefinitionKey())) {
            throw new BusinessException(ErrorCode.TASK_DEFINITION_MISMATCH);
        }
    }
}
