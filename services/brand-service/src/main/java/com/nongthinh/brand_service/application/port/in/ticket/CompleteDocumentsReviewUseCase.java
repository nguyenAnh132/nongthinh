package com.nongthinh.brand_service.application.port.in.ticket;

import com.nongthinh.brand_service.application.command.CompleteDocumentsReviewCommand;

public interface CompleteDocumentsReviewUseCase {

    void execute(String taskId, CompleteDocumentsReviewCommand command);
}
