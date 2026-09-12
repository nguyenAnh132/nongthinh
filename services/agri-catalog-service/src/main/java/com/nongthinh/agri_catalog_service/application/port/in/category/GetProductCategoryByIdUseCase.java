package com.nongthinh.agri_catalog_service.application.port.in.category;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;

public interface GetProductCategoryByIdUseCase {

    ProductCategoryView execute(UUID id);
}
