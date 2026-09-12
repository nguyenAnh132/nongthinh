package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.command.admin.RecordBrandVerificationLogCommand;
import com.nongthinh.profile_service.application.view.BrandVerificationLogView;

public interface RecordBrandVerificationLogUseCase {

    BrandVerificationLogView execute(RecordBrandVerificationLogCommand command);
}
