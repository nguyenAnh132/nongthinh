package com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelruntime.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelRuntimeWarmupCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelruntime.WarmModelRuntimeUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.service.ModelRuntimeManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarmModelRuntimeUseCaseImpl implements WarmModelRuntimeUseCase {

    private final ModelRuntimeManager modelRuntimeManager;

    @Override
    public boolean execute(ModelRuntimeWarmupCommand command) {
        return modelRuntimeManager.warm(command);
    }
}
