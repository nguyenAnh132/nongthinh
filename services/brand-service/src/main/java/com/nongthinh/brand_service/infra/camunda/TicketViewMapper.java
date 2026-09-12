package com.nongthinh.brand_service.infra.camunda;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.TaskService;
import com.nongthinh.brand_service.application.view.TicketView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketViewMapper {

    private final TaskService taskService;
    private final RuntimeService runtimeService;

    public TicketView toView(Task task) {
        Map<String, Object> variables = new HashMap<>(taskService.getVariables(task.getId()));
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        String businessKey = processInstance != null ? processInstance.getBusinessKey() : null;
        return new TicketView(
                task.getId(),
                task.getTaskDefinitionKey(),
                task.getName(),
                task.getProcessInstanceId(),
                businessKey,
                task.getAssignee(),
                toInstant(task.getCreateTime()),
                variables);
    }

    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
