package com.nongthinh.brand_service.infra.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.port.in.workflow.RequestDocumentsUseCase;
import com.nongthinh.brand_service.infra.camunda.CamundaExecutionSupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component("requestDocumentsDelegate")
public class RequestDocumentsDelegate implements JavaDelegate {

    private final RequestDocumentsUseCase requestDocumentsUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        requestDocumentsUseCase.execute(
                CamundaExecutionSupport.requireBrandProfileId(execution),
                CamundaExecutionSupport.resolveReviewerId(execution)
        );
    }
}
