package com.nongthinh.agri_catalog_service.application.port.in.category;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductCategoryUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;

public interface UpdateProductCategoryUseCase {

    ProductCategoryView execute(UUID id, ProductCategoryUpdateCommand command);
}
