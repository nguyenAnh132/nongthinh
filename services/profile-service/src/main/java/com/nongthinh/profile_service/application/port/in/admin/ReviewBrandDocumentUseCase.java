package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.command.admin.ReviewBrandDocumentCommand;
import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface ReviewBrandDocumentUseCase {

    BrandProfileView execute(ReviewBrandDocumentCommand command);
}
