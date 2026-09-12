package com.nongthinh.agri_catalog_service.application.port.in.category;

import java.util.UUID;

public interface DeleteProductCategoryUseCase {

    void execute(UUID id);
}
