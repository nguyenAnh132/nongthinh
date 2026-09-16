package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.PublicProductDetailView;

public interface GetPublicProductUseCase {
    PublicProductDetailView execute(UUID productId);
}
