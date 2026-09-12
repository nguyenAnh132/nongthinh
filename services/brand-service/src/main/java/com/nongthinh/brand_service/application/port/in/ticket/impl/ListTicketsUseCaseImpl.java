package com.nongthinh.brand_service.application.port.in.ticket.impl;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import com.nongthinh.brand_service.application.port.in.ticket.ListTicketsUseCase;
import com.nongthinh.brand_service.application.view.TicketView;
import com.nongthinh.brand_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.brand_service.infra.camunda.CamundaProcessConstants;
import com.nongthinh.brand_service.infra.camunda.TicketViewMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListTicketsUseCaseImpl implements ListTicketsUseCase {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;
    private final TicketViewMapper ticketViewMapper;

    @Override
    public List<TicketView> execute() {
        String userId = currentUserProvider.getCurrentUser().getUserId().toString();

        List<Task> assignedTasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();

        List<Task> candidateTasks = taskService.createTaskQuery()
                .taskCandidateGroup(CamundaProcessConstants.TICKET_CANDIDATE_GROUP)
                .taskUnassigned()
                .orderByTaskCreateTime()
                .desc()
                .list();

        Map<String, Task> tasksById = new LinkedHashMap<>();
        for (Task task : assignedTasks) {
            tasksById.put(task.getId(), task);
        }
        for (Task task : candidateTasks) {
            tasksById.putIfAbsent(task.getId(), task);
        }

        return tasksById.values().stream()
                .sorted(Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ticketViewMapper::toView)
                .toList();
    }
}
