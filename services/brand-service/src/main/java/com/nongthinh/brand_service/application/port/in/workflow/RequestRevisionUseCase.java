package com.nongthinh.brand_service.application.port.in.workflow;

import java.util.UUID;

public interface RequestRevisionUseCase {

    void execute(UUID brandProfileId, UUID actorUserId, String revisionReason);
}
