package com.nongthinh.post_service.application.port.in.postreport;

import com.nongthinh.post_service.application.command.CreatePostReportCommand;
import com.nongthinh.post_service.application.view.PostReportView;
import java.util.UUID;

public interface CreatePostReportUseCase {
    PostReportView execute(UUID postId, CreatePostReportCommand command);
}
