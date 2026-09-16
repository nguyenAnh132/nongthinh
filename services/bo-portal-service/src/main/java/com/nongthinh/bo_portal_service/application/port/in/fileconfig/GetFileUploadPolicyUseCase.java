package com.nongthinh.bo_portal_service.application.port.in.fileconfig;

import com.nongthinh.bo_portal_service.application.view.FileUploadPolicyView;

public interface GetFileUploadPolicyUseCase {
    FileUploadPolicyView execute(String purpose);
}
