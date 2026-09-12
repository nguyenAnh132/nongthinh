package com.nongthinh.brand_service.infra.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.in.workflow.RejectBrandUseCase;
import com.nongthinh.brand_service.infra.camunda.CamundaExecutionSupport;
import lombok.RequiredArgsConstructor;

@Component("rejectBrandDelegate")
@RequiredArgsConstructor
public class RejectBrandDelegate implements JavaDelegate {

    private final RejectBrandUseCase rejectBrandUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        rejectBrandUseCase.execute(
                CamundaExecutionSupport.requireBrandProfileId(execution),
                CamundaExecutionSupport.resolveReviewerId(execution),
                CamundaExecutionSupport.resolveRejectionReason(execution)
        );
    }
}
