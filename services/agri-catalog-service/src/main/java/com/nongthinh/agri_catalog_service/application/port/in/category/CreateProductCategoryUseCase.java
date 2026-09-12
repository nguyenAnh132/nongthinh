package com.nongthinh.agri_catalog_service.application.port.in.category;

import com.nongthinh.agri_catalog_service.application.command.ProductCategoryCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;

public interface CreateProductCategoryUseCase {

    ProductCategoryView execute(ProductCategoryCreationCommand command);
}
