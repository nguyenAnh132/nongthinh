package com.nongthinh.agri_catalog_service.application.port.in.productimage;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;

public interface ListProductImagesUseCase {

    List<ProductImageView> execute(UUID productId, boolean publicOnly);
}
