package com.nongthinh.brand_service.application.port.in.ticket.impl;

import java.util.UUID;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import com.nongthinh.brand_service.application.port.in.ticket.GetTicketUseCase;
import com.nongthinh.brand_service.application.port.in.workflow.GetBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.application.view.BrandApprovalProcessView;
import com.nongthinh.brand_service.application.view.TicketDetailView;
import com.nongthinh.brand_service.application.view.TicketView;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.camunda.CamundaTaskSupport;
import com.nongthinh.brand_service.infra.camunda.TicketViewMapper;
import com.nongthinh.brand_service.infra.client.profileservice.AdminBrandProfileDetailDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTicketUseCaseImpl implements GetTicketUseCase {

    private final CamundaTaskSupport camundaTaskSupport;
    private final TicketViewMapper ticketViewMapper;
    private final ProfileServicePort profileServicePort;
    private final GetBrandApprovalProcessUseCase getBrandApprovalProcessUseCase;

    @Override
    public TicketDetailView execute(String taskId) {
        Task task = camundaTaskSupport.requireTask(taskId);
        TicketView ticket = ticketViewMapper.toView(task);
        UUID brandProfileId = camundaTaskSupport.requireBrandProfileId(task);

        AdminBrandProfileDetailDto profileDetail = profileServicePort.getBrandProfileDetail(brandProfileId);
        BrandApprovalProcessView approvalProcess = loadApprovalProcess(brandProfileId);

        return new TicketDetailView(ticket, profileDetail, approvalProcess);
    }

    private BrandApprovalProcessView loadApprovalProcess(UUID brandProfileId) {
        try {
            return getBrandApprovalProcessUseCase.execute(brandProfileId);
        } catch (BusinessException ex) {
            if (ex.getErrorCode() == ErrorCode.APPROVAL_PROCESS_NOT_FOUND) {
                return null;
            }
            throw ex;
        }
    }
}
