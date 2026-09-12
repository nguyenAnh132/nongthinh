package com.nongthinh.agri_catalog_service.application.port.in.productimage;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductImageUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;

public interface UpdateProductImageUseCase {

    ProductImageView execute(UUID productId, UUID imageId, ProductImageUpdateCommand command);
}
