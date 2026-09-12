package com.nongthinh.agri_catalog_service.application.port.in.productimage;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;

public interface GetProductImageUseCase {

    ProductImageView execute(UUID productId, UUID imageId);
}
