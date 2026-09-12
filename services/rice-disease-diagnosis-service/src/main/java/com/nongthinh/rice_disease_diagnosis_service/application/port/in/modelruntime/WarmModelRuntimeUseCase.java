package com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelruntime;

import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelRuntimeWarmupCommand;

public interface WarmModelRuntimeUseCase {
    boolean execute(ModelRuntimeWarmupCommand command);
}
