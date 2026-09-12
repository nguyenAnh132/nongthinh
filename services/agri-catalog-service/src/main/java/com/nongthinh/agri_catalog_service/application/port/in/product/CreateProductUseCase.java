package com.nongthinh.agri_catalog_service.application.port.in.product;

import com.nongthinh.agri_catalog_service.application.command.ProductCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductView;

public interface CreateProductUseCase {

    ProductView execute(ProductCreationCommand command);
}
