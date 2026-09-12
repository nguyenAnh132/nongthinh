package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductView;

public interface ListProductsUseCase {

    List<ProductView> execute(UUID brandId, UUID categoryId, boolean publishedOnly);
}
