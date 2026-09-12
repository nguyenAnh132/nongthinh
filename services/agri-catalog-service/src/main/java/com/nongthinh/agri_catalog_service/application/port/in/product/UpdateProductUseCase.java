package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductView;

public interface UpdateProductUseCase {

    ProductView execute(UUID id, ProductUpdateCommand command);
}
