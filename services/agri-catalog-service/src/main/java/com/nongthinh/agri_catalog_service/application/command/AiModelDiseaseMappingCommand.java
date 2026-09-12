package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record AiModelDiseaseMappingCommand(
        UUID modelVersionClassId,
        UUID cropTypeId,
        UUID diseaseId
) {
}
