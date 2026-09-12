package com.nongthinh.profile_service.application.port.in.brand;

import com.nongthinh.profile_service.application.view.BrandDocumentView;

public interface UploadMyBrandDocumentUseCase {

    BrandDocumentView execute(String businessLicenseUrl);
}
