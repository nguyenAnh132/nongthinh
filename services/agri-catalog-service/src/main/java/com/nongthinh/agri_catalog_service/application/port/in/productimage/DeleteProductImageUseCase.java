package com.nongthinh.agri_catalog_service.application.port.in.productimage;

import java.util.UUID;

public interface DeleteProductImageUseCase {

    void execute(UUID productId, UUID imageId);
}
