package com.nongthinh.agri_catalog_service.application.port.in.product;

import com.nongthinh.agri_catalog_service.application.query.PublicProductQuery;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.application.view.ProductView;

public interface SearchPublicProductsUseCase {
    PageView<ProductView> execute(PublicProductQuery query);
}
