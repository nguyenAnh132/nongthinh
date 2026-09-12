package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductView;

public interface GetProductByIdUseCase {

    ProductView execute(UUID id);
}
