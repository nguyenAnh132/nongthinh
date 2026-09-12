package com.nongthinh.rice_disease_diagnosis_service.application.command;

import java.util.List;
import java.util.UUID;

public record DiagnosisCommand(UUID cropTypeId, List<UUID> fileIds) {
}
