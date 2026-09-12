package com.nongthinh.profile_service.application.port.in.admin;

import java.util.UUID;

public interface RecordBrandDocumentsRequestedUseCase {

    void execute(UUID brandProfileId, UUID actorUserId);
}
