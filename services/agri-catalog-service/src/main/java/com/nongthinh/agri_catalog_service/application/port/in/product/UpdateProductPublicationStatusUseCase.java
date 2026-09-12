package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;

public interface UpdateProductPublicationStatusUseCase {

    ProductView execute(UUID id, PublicationStatus publicationStatus);
}
