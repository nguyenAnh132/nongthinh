package com.nongthinh.brand_service.application.port.in.workflow;

import java.util.UUID;

public interface CorrelateDocumentsSubmittedUseCase {

    void execute(UUID brandProfileId);
}
