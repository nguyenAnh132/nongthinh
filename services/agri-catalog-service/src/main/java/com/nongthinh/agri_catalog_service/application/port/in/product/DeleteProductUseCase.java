package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.UUID;

public interface DeleteProductUseCase {

    void execute(UUID id);
}
