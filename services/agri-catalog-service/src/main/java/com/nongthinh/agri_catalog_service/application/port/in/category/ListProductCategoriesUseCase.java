package com.nongthinh.agri_catalog_service.application.port.in.category;

import java.util.List;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;

public interface ListProductCategoriesUseCase {

    List<ProductCategoryView> execute(boolean activeOnly);
}
