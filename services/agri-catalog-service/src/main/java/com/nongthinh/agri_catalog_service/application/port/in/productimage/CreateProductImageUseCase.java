package com.nongthinh.agri_catalog_service.application.port.in.productimage;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductImageCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;

public interface CreateProductImageUseCase {

    ProductImageView execute(UUID productId, ProductImageCreationCommand command);
}
