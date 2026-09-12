package com.nongthinh.profile_service.application.port.in.brand;

import java.util.Optional;
import com.nongthinh.profile_service.application.view.BrandDocumentView;

public interface GetMyBrandDocumentUseCase {

    Optional<BrandDocumentView> execute();
}
