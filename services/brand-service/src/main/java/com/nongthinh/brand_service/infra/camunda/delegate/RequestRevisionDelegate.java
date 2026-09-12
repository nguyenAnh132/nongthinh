package com.nongthinh.brand_service.infra.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.in.workflow.RequestRevisionUseCase;
import com.nongthinh.brand_service.infra.camunda.CamundaExecutionSupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component("requestRevisionDelegate")
public class RequestRevisionDelegate implements JavaDelegate {

    private final RequestRevisionUseCase requestRevisionUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        requestRevisionUseCase.execute(
                CamundaExecutionSupport.requireBrandProfileId(execution),
                CamundaExecutionSupport.resolveReviewerId(execution),
                CamundaExecutionSupport.resolveRevisionReason(execution)
        );
    }
}
