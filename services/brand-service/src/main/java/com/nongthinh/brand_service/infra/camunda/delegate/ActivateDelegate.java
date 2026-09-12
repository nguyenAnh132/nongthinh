package com.nongthinh.brand_service.infra.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.in.workflow.ActivateBrandUseCase;
import com.nongthinh.brand_service.infra.camunda.CamundaExecutionSupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component("activateDelegate")
public class ActivateDelegate implements JavaDelegate {

    private final ActivateBrandUseCase activateBrandUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        activateBrandUseCase.execute(
                CamundaExecutionSupport.requireBrandProfileId(execution),
                CamundaExecutionSupport.resolveReviewerId(execution)
        );
    }
}
