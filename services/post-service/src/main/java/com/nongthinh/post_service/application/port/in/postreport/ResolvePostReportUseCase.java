package com.nongthinh.post_service.application.port.in.postreport;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.view.PostReportView;
import java.util.UUID;

public interface ResolvePostReportUseCase {
    PostReportView execute(UUID reportId, CompletePostReportCommand command);
}
